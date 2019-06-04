package org.carlspring.strongbox.controllers.configuration.security.authorization;

import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
import org.carlspring.strongbox.authorization.dto.PrivilegeDto;
import org.carlspring.strongbox.authorization.dto.RoleDto;
import org.carlspring.strongbox.authorization.service.AuthorizationConfigService;
import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.forms.PrivilegeListForm;
import org.carlspring.strongbox.forms.RoleForm;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.service.impl.StrongboxUserService.StrongboxUserServiceQualifier;
import org.carlspring.strongbox.validation.RequestBodyValidationException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.function.Supplier;

import io.swagger.annotations.*;
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
import static org.carlspring.strongbox.net.MediaType.APPLICATION_YAML_VALUE;

/**
 * @author Pablo Tirado
 */
@Controller
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping(value = "/api/configuration/authorization")
@Api(value = "/api/configuration/authorization")
public class AuthorizationConfigController
        extends BaseController
{

    static final String SUCCESSFUL_ADD_ROLE = "The role was created successfully.";
    static final String FAILED_ADD_ROLE = "Role cannot be saved because the submitted form contains errors!";

    static final String SUCCESSFUL_GET_CONFIG = "Everything went ok.";
    static final String FAILED_GET_CONFIG = "Could not retrieve the strongbox-authorization.yaml configuration file.";

    static final String SUCCESSFUL_DELETE_ROLE = "The role was deleted.";
    static final String FAILED_DELETE_ROLE = "Could not delete the role.";

    static final String SUCCESSFUL_ASSIGN_PRIVILEGES = "The privileges were assigned.";
    static final String FAILED_ASSIGN_PRIVILEGES = "Privileges cannot be saved because the submitted form contains errors!";

    static final String AUTHORIZATION_CONFIG_OPERATION_FAILED = "Error during config processing.";

    @Inject
    private AuthorizationConfigService authorizationConfigService;

    @Inject
    @StrongboxUserServiceQualifier
    private UserService userService;

    @Inject
    private AnonymousAuthenticationFilter anonymousAuthenticationFilter;

    @Inject
    private ConversionService conversionService;


    @ApiOperation(value = "Used to add new roles")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_ADD_ROLE),
                            @ApiResponse(code = 400, message = FAILED_ADD_ROLE) })
    @PostMapping(value = "/role",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity addRole(@RequestBody @Validated RoleForm roleForm,
                                  BindingResult bindingResult,
                                  @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_ADD_ROLE, bindingResult);
        }

        RoleDto role = conversionService.convert(roleForm, RoleDto.class);

        authorizationConfigService.addRole(role);

        return processConfig(() -> SUCCESSFUL_ADD_ROLE, acceptHeader);
    }

    @ApiOperation(value = "Retrieves the strongbox-authorization.yaml configuration file.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_GET_CONFIG),
                            @ApiResponse(code = 500, message = FAILED_GET_CONFIG) })
    @GetMapping(produces = { APPLICATION_YAML_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getAuthorizationConfig(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        return processConfig(null, acceptHeader);
    }

    @ApiOperation(value = "Deletes a role by name.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_DELETE_ROLE),
                            @ApiResponse(code = 400, message = FAILED_DELETE_ROLE) })
    @DeleteMapping(value = "/role/{name}",
                   produces = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity deleteRole(@ApiParam(value = "The name of the role", required = true)
                                     @PathVariable("name") String name,
                                     @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        try
        {
            deleteRole(name);
        }
        catch (RuntimeException e)
        {
            String message = e.getMessage();
            logger.error(message);
            return getBadRequestResponseEntity(message, acceptHeader);
        }

        return processConfig(() -> SUCCESSFUL_DELETE_ROLE, acceptHeader);
    }

    private void deleteRole(String name)
    {
        if (authorizationConfigService.deleteRole(name))
        {
            // revoke role from every user that exists in the system
            userService.revokeEveryone(name.toUpperCase());
        }
        else
        {
            throw new RuntimeException(FAILED_DELETE_ROLE);
        }
    }

    @ApiOperation(value = "Used to assign privileges to the anonymous user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_ASSIGN_PRIVILEGES),
                            @ApiResponse(code = 400, message = FAILED_ASSIGN_PRIVILEGES) })
    @PostMapping(value = "/anonymous/privileges",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity addPrivilegesToAnonymous(@RequestBody @Validated PrivilegeListForm privilegeListForm,
                                                   BindingResult bindingResult,
                                                   @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_ASSIGN_PRIVILEGES, bindingResult);
        }

        TypeDescriptor sourceType = TypeDescriptor.valueOf(PrivilegeListForm.class);
        TypeDescriptor targetType = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(PrivilegeDto.class));
        List<PrivilegeDto> privilegeList = (List<PrivilegeDto>) conversionService.convert(privilegeListForm,
                                                                                          sourceType,
                                                                                          targetType);

        authorizationConfigService.addPrivilegesToAnonymous(privilegeList);
        addAnonymousAuthority(privilegeList);

        return processConfig(() -> SUCCESSFUL_ASSIGN_PRIVILEGES, acceptHeader);
    }

    private ResponseEntity processConfig(Supplier<String> successMessage,
                                         String acceptHeader)
    {
        return processConfig(successMessage, ResponseEntity::ok, acceptHeader);
    }

    private ResponseEntity processConfig(Supplier<String> successMessage,
                                         CustomSuccessResponseBuilder customSuccessResponseBuilder,
                                         String acceptHeader)
    {
        try
        {
            if (successMessage != null)
            {
                return getSuccessfulResponseEntity(successMessage.get(), acceptHeader);

            }
            else
            {
                return customSuccessResponseBuilder.build(authorizationConfigService.getDto());
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

    private void addAnonymousAuthority(List<PrivilegeDto> authorities)
    {
        authorities.stream().map(PrivilegeDto::getName).forEach(this::addAnonymousAuthority);
    }

    private void addAnonymousAuthority(String authority)
    {
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(authority.toUpperCase());
        anonymousAuthenticationFilter.getAuthorities().add(simpleGrantedAuthority);
    }

    private interface CustomSuccessResponseBuilder
    {
        ResponseEntity build(AuthorizationConfigDto config)throws JAXBException;
    }

}
