package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.security.jaas.Privilege;
import org.carlspring.strongbox.security.jaas.Role;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.security.AuthorizationConfig;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.stereotype.Component;

/**
 * Defines REST API for managing security configuration: privileges, roles etc..
 *
 * @author Alex Oreshkevich
 */
@Component
@Path("/configuration/authorization")
@Api(value = "/configuration/authorization")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
@PreAuthorize("hasAuthority('ADMIN')")
public class AuthorizationConfigRestlet
        extends BaseArtifactRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationConfigRestlet.class);
    private final GenericParser<AuthorizationConfig> configGenericParser = new GenericParser<>(AuthorizationConfig.class);

    @Autowired
    AuthorizationConfigProvider configProvider;

    @Autowired
    UserService userService;

    @Autowired
    OObjectDatabaseTx databaseTx;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    AnonymousAuthenticationFilter anonymousAuthenticationFilter;

    private volatile AuthorizationConfig config;

    private synchronized Response processConfig(Consumer<AuthorizationConfig> consumer)
    {
        return processConfig(consumer, config -> Response.ok().build());
    }

    private synchronized Response processConfig(Consumer<AuthorizationConfig> consumer,
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
    @POST
    @Path("role")
    @ApiOperation(value = "Used to add new roles")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The role was created successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    public synchronized Response addRole(String json)
    {
        logger.debug("Trying to add new role from JSON\n" + json);
        return processConfig(config ->
                             {
                                 Role role = read(json, Role.class);
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
    @GET
    @Path("/xml")
    @Produces({ MediaType.APPLICATION_XML,
                MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieves the security-authorization.xml configuration file.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    public synchronized Response getAuthorizationConfig()
    {
        logger.debug("Trying to receive authorization config as XML file...");

        // TODO Due to internal error in spring-data-orientdb
        // com.orientechnologies.orient.client.remote.OStorageRemote cannot be cast to
        // com.orientechnologies.orient.core.storage.impl.local.paginated.OLocalPaginatedStorage
        return processConfig(null, config -> Response.ok(configGenericParser.serialize(config)).build());
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Revoke role by name
    @DELETE
    @Path("role/{name}")
    @ApiOperation(value = "Deletes a role by name.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The role was deleted."),
                            @ApiResponse(code = 400, message = "Bad request.")
    })
    public Response deleteRole(@ApiParam(value = "The name of the role", required = true)
                               @PathParam("name") String name)
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
    @POST
    @Path("anonymous/privileges")
    @Consumes(MediaType.APPLICATION_JSON)
    public synchronized Response addPrivilegesToAnonymous(List<Privilege> privileges)
    {
        return processConfig(config -> privileges.forEach(this::addAnonymousAuthority));
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Assign roles to the anonymous user
    @POST
    @Path("anonymous/roles")
    @Consumes(MediaType.APPLICATION_JSON)
    public synchronized Response addRolesToAnonymous(List<Role> roles)
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

        Response build(AuthorizationConfig config)
                throws JAXBException;
    }
}
