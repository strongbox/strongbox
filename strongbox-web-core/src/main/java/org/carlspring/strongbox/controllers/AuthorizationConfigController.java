package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.security.Privilege;
import org.carlspring.strongbox.security.Role;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.security.AuthorizationConfig;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;
import org.carlspring.strongbox.users.service.UserService;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.swagger.annotations.*;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author Pablo Tirado
 */
@Controller
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping(value = "/configuration/authorization")
@Api(value = "/configuration/authorization")
public class AuthorizationConfigController
        extends BaseArtifactController
{

    static final String SUCCESSFUL_ADD_ROLE = "The role was created successfully.";
    static final String FAILED_ADD_ROLE = "Could not add a new role.";

    static final String SUCCESSFUL_DELETE_ROLE = "The role was deleted.";
    static final String FAILED_DELETE_ROLE = "Could not delete the role.";

    static final String SUCCESSFUL_ASSIGN_PRIVILEGES = "The privileges were assigned.";

    static final String SUCCESSFUL_ASSIGN_ROLES = "The roles were assigned.";

    @Inject
    private AuthorizationConfigProvider configProvider;

    @Inject
    private UserService userService;

    @Inject
    private CacheManager cacheManager;

    @Inject
    private AnonymousAuthenticationFilter anonymousAuthenticationFilter;

    // ----------------------------------------------------------------------------------------------------------------
    // Add role

    @ApiOperation(value = "Used to add new roles")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = SUCCESSFUL_ADD_ROLE),
                            @ApiResponse(code = 400,
                                         message = FAILED_ADD_ROLE) })
    @PostMapping(value = "/role",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = { MediaType.TEXT_PLAIN_VALUE,
                              MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity addRole(@RequestBody Role role,
                                  @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        return processConfig(config -> addRole(config, role), () -> SUCCESSFUL_ADD_ROLE, acceptHeader);
    }

    private void addRole(AuthorizationConfig config,
                         Role role)
    {

        boolean result = config.getRoles()
                               .getRoles()
                               .add(role);

        if (result)
        {
            configProvider.updateConfig(config);
        }
        else
        {
            throw new RuntimeException(FAILED_ADD_ROLE);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // View authorization config as XML file

    @ApiOperation(value = "Retrieves the strongbox-authorization.xml configuration file.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "Everything went ok."),
                            @ApiResponse(code = 500,
                                         message = "Could not retrieve the strongbox-authorization.xml configuration file.") })
    @GetMapping(value = "/xml",
            produces = { MediaType.APPLICATION_XML_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getAuthorizationConfig(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        return processConfig(null, null, acceptHeader);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Revoke role by name

    @ApiOperation(value = "Deletes a role by name.",
                  position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = SUCCESSFUL_DELETE_ROLE),
                            @ApiResponse(code = 400,
                                         message = FAILED_DELETE_ROLE)
    })
    @DeleteMapping(value = "/role/{name}",
                   consumes = MediaType.APPLICATION_JSON_VALUE,
                   produces = { MediaType.TEXT_PLAIN_VALUE,
                                MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity deleteRole(@ApiParam(value = "The name of the role",
                                               required = true)
                                     @PathVariable("name") String name,
                                     @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        return processConfig(config -> deleteRole(config, name), () -> SUCCESSFUL_DELETE_ROLE, acceptHeader);
    }

    private void deleteRole(AuthorizationConfig config,
                            String name)
    {
        // find Privilege by name
        Role target = null;
        for (Role role : config.getRoles()
                               .getRoles())
        {
            if (role.getName()
                    .equalsIgnoreCase(name))
            {
                target = role;
                break;
            }
        }
        if (target != null)
        {
            // revoke role from current config
            config.getRoles()
                  .getRoles()
                  .remove(target);
            configProvider.updateConfig(config);

            // revoke role from every user that exists in the system
            getAllUsers().forEach(user ->
                                  {
                                      if (user.getRoles().remove(name.toUpperCase()))
                                      {
                                          // evict such kind of users from cache
                                          Objects.requireNonNull(
                                                  cacheManager.getCache(CacheName.User.USERS)).evict(
                                                  user.getUsername());
                                          Objects.requireNonNull(cacheManager.getCache(
                                                  CacheName.User.USER_DETAILS)).evict(
                                                  user.getUsername());
                                      }
                                  });
        }
        else
        {
            throw new RuntimeException(FAILED_DELETE_ROLE);
        }
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Assign privileges to the anonymous user

    @ApiOperation(value = "Used to assign privileges to the anonymous user")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = SUCCESSFUL_ASSIGN_PRIVILEGES) })
    @PostMapping(value = "/anonymous/privileges",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = { MediaType.TEXT_PLAIN_VALUE,
                              MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity addPrivilegesToAnonymous(@RequestBody List<Privilege> privileges,
                                                   @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        return processConfig(config -> privileges.forEach(this::addAnonymousAuthority),
                             () -> SUCCESSFUL_ASSIGN_PRIVILEGES, acceptHeader);
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Assign roles to the anonymous user

    @ApiOperation(value = "Used to assign roles to the anonymous user")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = SUCCESSFUL_ASSIGN_ROLES) })
    @PostMapping(value = "/anonymous/roles",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = { MediaType.TEXT_PLAIN_VALUE,
                              MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity addRolesToAnonymous(@RequestBody List<Role> roles,
                                              @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        return processConfig(config -> addRolesToAnonymous(config, roles), () -> SUCCESSFUL_ASSIGN_ROLES, acceptHeader);
    }

    private void addRolesToAnonymous(AuthorizationConfig config,
                                     List<Role> roles)
    {
        roles.forEach(role -> config.getRoles()
                                    .getRoles()
                                    .stream()
                                    .filter(
                                            role1 -> role1.getName()
                                                          .equalsIgnoreCase(
                                                                  role.getName()))
                                    .forEach(
                                            foundedRole -> foundedRole.getPrivileges()
                                                                      .forEach(
                                                                              this::addAnonymousAuthority)));

    }

    private ResponseEntity processConfig(Consumer<AuthorizationConfig> consumer,
                                         Supplier<String> supplier,
                                         String acceptHeader)
    {
        return processConfig(consumer, supplier, ResponseEntity::ok, acceptHeader);
    }

    private ResponseEntity processConfig(Consumer<AuthorizationConfig> consumer,
                                         Supplier<String> supplier,
                                         CustomSuccessResponseBuilder customSuccessResponseBuilder,
                                         String acceptHeader)
    {
        Optional<AuthorizationConfig> configOptional = configProvider.getConfig();

        if (configOptional.isPresent())
        {
            try
            {
                AuthorizationConfig config = configOptional.get();

                if (consumer != null)
                {
                    consumer.accept(config);
                }

                if (supplier != null)
                {
                    return getSuccessfulResponseEntity(supplier.get(), acceptHeader);

                }
                else
                {
                    return customSuccessResponseBuilder.build(config);
                }
            }
            catch (RuntimeException e)
            {
                String message = e.getMessage();
                return getExceptionResponseEntity(HttpStatus.BAD_REQUEST, message, e, acceptHeader);
            }
            catch (Exception e)
            {
                String message = "Error during config processing.";
                return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, message, e, acceptHeader);
            }
        }
        else
        {
            String message = "Unable to locate AuthorizationConfig to update...";
            return getRuntimeExceptionResponseEntity(message, acceptHeader);
        }
    }

    private void addAnonymousAuthority(Privilege authority)
    {
        addAnonymousAuthority(authority.getName());
    }

    private void addAnonymousAuthority(String authority)
    {
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(authority.toUpperCase());
        anonymousAuthenticationFilter.getAuthorities()
                                     .add(simpleGrantedAuthority);
    }

    private List<User> getAllUsers()
    {
        return userService.findAll()
                          .orElse(new LinkedList<>());
    }

    private interface CustomSuccessResponseBuilder
    {

        ResponseEntity build(AuthorizationConfig config)
                throws JAXBException;
    }

}