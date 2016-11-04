package org.carlspring.strongbox.controller;

import org.carlspring.strongbox.security.Privilege;
import org.carlspring.strongbox.security.Role;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.security.AuthorizationConfig;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/configuration/authorization")
@PreAuthorize("hasAuthority('ADMIN')")
public class AuthorizationConfigController
        extends BaseArtifactController
{

    private final GenericParser<AuthorizationConfig> configGenericParser = new GenericParser<>(AuthorizationConfig.class);

    @Inject
    AuthorizationConfigProvider configProvider;

    @Inject
    UserService userService;

    @Inject
    OObjectDatabaseTx databaseTx;

    @Inject
    CacheManager cacheManager;

    @Inject
    AnonymousAuthenticationFilter anonymousAuthenticationFilter;

    private volatile AuthorizationConfig config;

    private synchronized ResponseEntity processConfig(Consumer<AuthorizationConfig> consumer)
    {
        return processConfig(consumer, config -> ResponseEntity.ok().build());
    }

    private synchronized ResponseEntity processConfig(Consumer<AuthorizationConfig> consumer,
                                                      CustomSuccessResponseBuilder customSuccessResponseBuilder)
    {
        databaseTx.activateOnCurrentThread();
        Optional<AuthorizationConfig> configOptional = configProvider.getConfig();

        if (configOptional.isPresent())
        {
            try
            {
                config = databaseTx.detachAll(configOptional.get(), true);

                if (consumer != null)
                {
                    consumer.accept(config);
                }

                return customSuccessResponseBuilder.build(config);
            }
            catch (Exception e)
            {
                logger.error("Error during config processing.", e);
                return toError("Error during config processing: " + e.getLocalizedMessage());
            }
        }
        else
        {
            return toError("Unable to locate AuthorizationConfig to update...");
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Add role

    @ApiOperation(value = "Used to add new roles")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The role was created successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @RequestMapping(value = "role", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
    public synchronized ResponseEntity addRole(@RequestBody String serializedJson)
            throws JAXBException
    {

        GenericParser<Role> parser = new GenericParser<>(Role.class);
        Role role = parser.deserialize(serializedJson);

        logger.debug("Trying to add new role from JSON\n" + serializedJson);
        logger.debug(role.toString());
        return processConfig(config ->
                             {
                                 //  Role role = read(json, Role.class);
                                 boolean result = config.getRoles().getRoles().add(role);

                                 if (result)
                                 {
                                     logger.debug("Successfully added new role " + role.getName());
                                     configProvider.updateConfig(config);
                                 }
                                 else
                                 {
                                     logger.warn("Unable to add new role " + role.getName());
                                 }
                             });
    }

    // ----------------------------------------------------------------------------------------------------------------
    // View authorization config as XML file

    @ApiOperation(value = "Retrieves the security-authorization.xml configuration file.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @RequestMapping(value = "/xml", method = RequestMethod.GET, produces = { MediaType.APPLICATION_XML_VALUE,
                                                                             MediaType.APPLICATION_JSON_VALUE })
    public synchronized ResponseEntity getAuthorizationConfig()
            throws JAXBException
    {
        logger.debug("Trying to receive authorization config as XML / JSON file...");

        return processConfig(null, ResponseEntity::ok);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Revoke role by name

    @ApiOperation(value = "Deletes a role by name.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The role was deleted."),
                            @ApiResponse(code = 400, message = "Bad request.")
    })
    @RequestMapping(value = "role/{name}", method = RequestMethod.DELETE)
    public ResponseEntity deleteRole(@ApiParam(value = "The name of the role", required = true)
                                     @PathVariable("name") String name)
            throws Exception
    {
        return processConfig(config ->
                             {

                                 // find Privilege by name
                                 Role target = null;
                                 for (Role role : config.getRoles().getRoles())
                                 {
                                     if (role.getName().equalsIgnoreCase(name))
                                     {
                                         target = role;
                                         break;
                                     }
                                 }
                                 if (target != null)
                                 {
                                     // revoke role from current config
                                     config.getRoles().getRoles().remove(target);
                                     configProvider.updateConfig(config);

                                     // revoke role from every user that exists in the system
                                     getAllUsers().forEach(user ->
                                                           {
                                                               if (user.getRoles().remove(name.toUpperCase()))
                                                               {
                                                                   // evict such kind of users from cache
                                                                   cacheManager.getCache("users").evict(user);
                                                               }
                                                           });
                                 }
                             });
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Assign privileges to the anonymous user

    @RequestMapping(value = "anonymous/privileges",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public synchronized ResponseEntity addPrivilegesToAnonymous(@RequestBody List<Privilege> privileges)
    {
        return processConfig(config -> privileges.forEach(this::addAnonymousAuthority));
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Assign roles to the anonymous user

    @RequestMapping(value = "anonymous/roles",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public synchronized ResponseEntity addRolesToAnonymous(List<Role> roles)
    {
        return processConfig(config -> roles.forEach(role -> config.getRoles().getRoles().stream().filter(
                role1 -> role1.getName().equalsIgnoreCase(role.getName())).forEach(
                foundedRole -> foundedRole.getPrivileges().forEach(this::addAnonymousAuthority))));
    }

    private void addAnonymousAuthority(Privilege authority)
    {
        addAnonymousAuthority(authority.getName());
    }

    private void addAnonymousAuthority(String authority)
    {
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(authority.toUpperCase());
        config.getAnonymousAuthorities().add(simpleGrantedAuthority);
        anonymousAuthenticationFilter.getAuthorities().add(simpleGrantedAuthority);
    }

    private synchronized List<User> getAllUsers()
    {
        final List<User> users = new LinkedList<>();
        databaseTx.activateOnCurrentThread();
        userService.findAll().ifPresent(
                usersList -> usersList.forEach(user -> users.add(databaseTx.detach(user, true))));
        return users;
    }

    private interface CustomSuccessResponseBuilder
    {

        ResponseEntity build(AuthorizationConfig config)
                throws JAXBException;
    }


}