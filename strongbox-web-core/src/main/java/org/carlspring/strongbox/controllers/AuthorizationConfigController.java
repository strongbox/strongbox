package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.forms.PrivilegeListForm;
import org.carlspring.strongbox.forms.RoleForm;
import org.carlspring.strongbox.forms.RoleListForm;
import org.carlspring.strongbox.security.Privilege;
import org.carlspring.strongbox.security.Role;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.security.AuthorizationConfig;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.validation.RequestBodyValidationException;

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
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Pablo Tirado
 */
@Controller
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping(value = "/api/configuration/authorization")
@Api(value = "/api/configuration/authorization")
public class AuthorizationConfigController
        extends BaseArtifactController
{

    static final String SUCCESSFUL_ADD_ROLE = "The role was created successfully.";
    static final String FAILED_ADD_ROLE = "Role cannot be saved because the submitted form contains errors!";

    static final String SUCCESSFUL_GET_CONFIG = "Everything went ok.";
    static final String FAILED_GET_CONFIG = "Could not retrieve the strongbox-authorization.xml configuration file.";

    static final String SUCCESSFUL_DELETE_ROLE = "The role was deleted.";
    static final String FAILED_DELETE_ROLE = "Could not delete the role.";

    static final String SUCCESSFUL_ASSIGN_PRIVILEGES = "The privileges were assigned.";
    static final String FAILED_ASSIGN_PRIVILEGES = "Privileges cannot be saved because the submitted form contains errors!";

    static final String SUCCESSFUL_ASSIGN_ROLES = "The roles were assigned.";
    static final String FAILED_ASSIGN_ROLES = "Roles cannot be saved because the submitted form contains errors!";

    static final String AUTHORIZATION_CONFIG_OPERATION_FAILED = "Error during config processing.";
    static final String AUTHORIZATION_CONFIG_NOT_FOUND = "Unable to locate AuthorizationConfig to update...";

    @Inject
    private AuthorizationConfigProvider configProvider;

    @Inject
    private UserService userService;

    @Inject
    private CacheManager cacheManager;

    @Inject
    private AnonymousAuthenticationFilter anonymousAuthenticationFilter;

    @Inject
    private ConversionService conversionService;

    @ApiOperation(value = "Used to add new roles")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = SUCCESSFUL_ADD_ROLE),
                            @ApiResponse(code = 400,
                                         message = FAILED_ADD_ROLE) })
    @PostMapping(value = "/role",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = { MediaType.TEXT_PLAIN_VALUE,
                              MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity addRole(@RequestBody @Validated RoleForm roleForm,
                                  BindingResult bindingResult,
                                  @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_ADD_ROLE, bindingResult);
        }

        Role role = conversionService.convert(roleForm, Role.class);

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
            configProvider.save(config);
        }
    }

    @ApiOperation(value = "Retrieves the strongbox-authorization.xml configuration file.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = SUCCESSFUL_GET_CONFIG),
                            @ApiResponse(code = 500,
                                         message = FAILED_GET_CONFIG) })
    @GetMapping(value = "/xml",
                produces = { MediaType.APPLICATION_XML_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getAuthorizationConfig(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        return processConfig(null, null, acceptHeader);
    }

    @ApiOperation(value = "Deletes a role by name.",
                  position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = SUCCESSFUL_DELETE_ROLE),
                            @ApiResponse(code = 400,
                                         message = FAILED_DELETE_ROLE)
    })
    @DeleteMapping(value = "/role/{name}",
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
            configProvider.save(config);

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


    @ApiOperation(value = "Used to assign privileges to the anonymous user")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = SUCCESSFUL_ASSIGN_PRIVILEGES),
                            @ApiResponse(code = 400,
                                         message = FAILED_ASSIGN_PRIVILEGES) })
    @PostMapping(value = "/anonymous/privileges",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = { MediaType.TEXT_PLAIN_VALUE,
                              MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity addPrivilegesToAnonymous(@RequestBody @Validated PrivilegeListForm privilegeListForm,
                                                   BindingResult bindingResult,
                                                   @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_ASSIGN_PRIVILEGES, bindingResult);
        }

        TypeDescriptor sourceType = TypeDescriptor.valueOf(PrivilegeListForm.class);
        TypeDescriptor targetType = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Privilege.class));
        List<Privilege> privilegeList = (List<Privilege>) conversionService.convert(privilegeListForm,
                                                                                    sourceType,
                                                                                    targetType);

        return processConfig(config -> Objects.requireNonNull(privilegeList).forEach(this::addAnonymousAuthority),
                             () -> SUCCESSFUL_ASSIGN_PRIVILEGES, acceptHeader);
    }


    @ApiOperation(value = "Used to assign roles to the anonymous user")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = SUCCESSFUL_ASSIGN_ROLES),
                            @ApiResponse(code = 400,
                                         message = FAILED_ASSIGN_ROLES) })
    @PostMapping(value = "/anonymous/roles",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = { MediaType.TEXT_PLAIN_VALUE,
                              MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity addRolesToAnonymous(@RequestBody @Validated RoleListForm roleListForm,
                                              BindingResult bindingResult,
                                              @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_ASSIGN_ROLES, bindingResult);
        }

        TypeDescriptor sourceType = TypeDescriptor.valueOf(RoleListForm.class);
        TypeDescriptor targetType = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Role.class));
        List<Role> roleList = (List<Role>) conversionService.convert(roleListForm,
                                                                     sourceType,
                                                                     targetType);

        return processConfig(config -> addRolesToAnonymous(config, Objects.requireNonNull(roleList)),
                             () -> SUCCESSFUL_ASSIGN_ROLES,
                             acceptHeader);
    }

    private void addRolesToAnonymous(AuthorizationConfig config,
                                     List<Role> roles)
    {
        roles.forEach(role -> config.getRoles()
                                    .getRoles()
                                    .stream()
                                    .filter(role1 -> role1.getName().equalsIgnoreCase(role.getName()))
                                    .forEach(foundedRole -> foundedRole.getPrivileges()
                                                                       .forEach(this::addAnonymousAuthority)));

    }

    private ResponseEntity processConfig(Consumer<AuthorizationConfig> authorizationConfigOperation,
                                         Supplier<String> successMessage,
                                         String acceptHeader)
    {
        return processConfig(authorizationConfigOperation, successMessage, ResponseEntity::ok, acceptHeader);
    }

    private ResponseEntity processConfig(Consumer<AuthorizationConfig> authorizationConfigOperation,
                                         Supplier<String> successMessage,
                                         CustomSuccessResponseBuilder customSuccessResponseBuilder,
                                         String acceptHeader)
    {
        Optional<AuthorizationConfig> configOptional = configProvider.get();

        if (configOptional.isPresent())
        {
            try
            {
                AuthorizationConfig config = configOptional.get();

                if (authorizationConfigOperation != null)
                {
                    authorizationConfigOperation.accept(config);
                }

                if (successMessage != null)
                {
                    return getSuccessfulResponseEntity(successMessage.get(), acceptHeader);

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
                return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  AUTHORIZATION_CONFIG_OPERATION_FAILED,
                                                  e,
                                                  acceptHeader);
            }
        }
        else
        {
            return getRuntimeExceptionResponseEntity(AUTHORIZATION_CONFIG_NOT_FOUND, acceptHeader);
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
