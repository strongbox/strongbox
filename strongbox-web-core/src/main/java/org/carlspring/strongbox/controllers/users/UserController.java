package org.carlspring.strongbox.controllers.users;

import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.forms.users.AccessModelForm;
import org.carlspring.strongbox.forms.users.UserForm;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.userdetails.SpringSecurityUser;
import org.carlspring.strongbox.validation.RequestBodyValidationException;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.lang.JoseException;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Pablo Tirado
 */
@Controller
@RequestMapping("/api/users")
@Api(value = "/api/users")
public class UserController
        extends BaseController
{

    static final String SUCCESSFUL_CREATE_USER = "The user was created successfully.";

    static final String FAILED_CREATE_USER = "User cannot be created because the submitted form contains errors!";

    static final String SUCCESSFUL_GET_USER = "User was retrieved successfully.";

    static final String NOT_FOUND_USER = "The specified user does not exist!";

    static final String SUCCESSFUL_GET_USERS = "Users were retrieved successfully.";

    static final String SUCCESSFUL_UPDATE_USER = "The user was updated successfully.";

    static final String FAILED_UPDATE_USER = "User cannot be updated because the submitted form contains errors!";

    static final String SUCCESSFUL_DELETE_USER = "The user was deleted.";

    static final String FAILED_DELETE_USER = "Could not delete the user.";

    static final String FAILED_DELETE_SAME_USER = "Unable to delete yourself";

    static final String SUCCESSFUL_GENERATE_SECURITY_TOKEN = "The security token was generated.";

    static final String FAILED_GENERATE_SECURITY_TOKEN = "Failed to generate SecurityToken";

    static final String SUCCESSFUL_GENERATE_AUTH_TOKEN = "The authentication token was generated.";

    static final String SUCCESSFUL_UPDATE_ACCESS_MODEL = "The custom access model was updated.";

    static final String FAILED_UPDATE_ACCESS_MODEL = "Could not update the access model.";

    @Inject
    private UserService userService;

    @Inject
    private ConversionService conversionService;


    /**
     * This method exists for testing purposes.
     */
    @ApiOperation(value = "Used to retrieve an request param", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "") })
    @PreAuthorize("authenticated")
    @GetMapping(value = "/{anyString}",
            produces = { MediaType.TEXT_PLAIN_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public ResponseEntity greet(@PathVariable String anyString,
                                @ApiParam(value = "The param name", required = true)
                                @RequestParam(value = "name", required = false) String param,
                                @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        logger.debug("Say hello to {}. Path variable: {}", param, anyString);

        return getSuccessfulResponseEntity("hello, " + param, accept);
    }

    @ApiOperation(value = "Used to create new user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_CREATE_USER),
                            @ApiResponse(code = 400, message = FAILED_CREATE_USER) })
    @PreAuthorize("hasAuthority('CREATE_USER')")
    @PostMapping(value = "/user",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = { MediaType.TEXT_PLAIN_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public ResponseEntity create(@RequestBody @Validated(UserForm.NewUser.class) UserForm userForm,
                                 BindingResult bindingResult,
                                 @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_CREATE_USER, bindingResult);
        }

        UserDto user = conversionService.convert(userForm, UserDto.class);
        userService.add(user);

        return getSuccessfulResponseEntity(SUCCESSFUL_CREATE_USER, accept);
    }

    @ApiOperation(value = "Used to retrieve an user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_GET_USER),
                            @ApiResponse(code = 404, message = NOT_FOUND_USER) })
    @PreAuthorize("hasAuthority('VIEW_USER') || #name == principal.username")
    @GetMapping(value = "user/{name}",
            produces = { MediaType.TEXT_PLAIN_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public ResponseEntity getUser(@ApiParam(value = "The name of the user", required = true) @PathVariable String name,
                                  @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        User user = userService.findByUserName(name);
        if (user == null)
        {
            return getNotFoundResponseEntity(NOT_FOUND_USER, accept);
        }

        UserOutput userOutput = UserOutput.fromUser(user);
        Object body = getUserOutputEntityBody(userOutput, accept);
        return ResponseEntity.ok(body);
    }

    @ApiOperation(value = "Used to retrieve all users")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_GET_USERS) })
    @PreAuthorize("hasAuthority('VIEW_USER')")
    @GetMapping(value = "/all",
            produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public ResponseEntity getUsers()
    {
        List<UserOutput> users = userService.findAll()
                                            .getUsers()
                                            .stream()
                                            .map(UserOutput::fromUser).collect(Collectors.toList());

        return getJSONListResponseEntityBody("users", users);
    }

    @ApiOperation(value = "Used to update user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_UPDATE_USER),
                            @ApiResponse(code = 400, message = FAILED_UPDATE_USER) })
    @PreAuthorize("hasAuthority('UPDATE_USER') || #userToUpdate.username == principal.username")
    @PutMapping(value = "user",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = { MediaType.TEXT_PLAIN_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public ResponseEntity update(@RequestBody @Validated(UserForm.ExistingUser.class) UserForm userToUpdate,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_UPDATE_USER, bindingResult);
        }

        if (!(authentication.getPrincipal() instanceof SpringSecurityUser))
        {
            String message = "Unsupported logged user principal type: " + authentication.getPrincipal().getClass();
            return getFailedResponseEntity(HttpStatus.BAD_REQUEST, message, accept);
        }

        UserDto user = conversionService.convert(userToUpdate, UserDto.class);
        final SpringSecurityUser loggedUser = (SpringSecurityUser) authentication.getPrincipal();
        if (StringUtils.equals(loggedUser.getUsername(), user.getUsername()))
        {
            userService.updatePassword(user);
        }
        else
        {
            userService.updateByUsername(user);
        }

        return getSuccessfulResponseEntity(SUCCESSFUL_UPDATE_USER, accept);
    }

    @ApiOperation(value = "Deletes a user from a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_DELETE_USER),
                            @ApiResponse(code = 400, message = FAILED_DELETE_USER),
                            @ApiResponse(code = 404, message = NOT_FOUND_USER) })
    @PreAuthorize("hasAuthority('DELETE_USER')")
    @DeleteMapping(value = "user/{name}",
            produces = { MediaType.TEXT_PLAIN_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public ResponseEntity delete(@ApiParam(value = "The name of the user") @PathVariable String name,
                                 Authentication authentication,
                                 @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        if (!(authentication.getPrincipal() instanceof SpringSecurityUser))
        {
            String message = "Unsupported logged user principal type: " + authentication.getPrincipal().getClass();
            return getFailedResponseEntity(HttpStatus.BAD_REQUEST, message, accept);
        }

        final SpringSecurityUser loggedUser = (SpringSecurityUser) authentication.getPrincipal();
        if (StringUtils.equals(loggedUser.getUsername(), name))
        {
            return getFailedResponseEntity(HttpStatus.BAD_REQUEST, FAILED_DELETE_SAME_USER, accept);
        }

        User user = userService.findByUserName(name);
        if (user == null)
        {
            return getNotFoundResponseEntity(NOT_FOUND_USER, accept);
        }

        userService.delete(user.getUsername());

        return getSuccessfulResponseEntity(SUCCESSFUL_DELETE_USER, accept);
    }

    @ApiOperation(value = "Generate new security token for specified user.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_GENERATE_SECURITY_TOKEN),
                            @ApiResponse(code = 400, message = FAILED_GENERATE_SECURITY_TOKEN),
                            @ApiResponse(code = 404, message = NOT_FOUND_USER) })
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @GetMapping(value = "user/{username}/generate-security-token",
            produces = { MediaType.TEXT_PLAIN_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity generateSecurityToken(@ApiParam(value = "The name of the user") @PathVariable String username,
                                                @RequestHeader(HttpHeaders.ACCEPT) String accept)
            throws JoseException
    {
        User user = userService.findByUserName(username);
        if (user == null)
        {
            return getNotFoundResponseEntity(NOT_FOUND_USER, accept);
        }

        String securityToken = userService.generateSecurityToken(username);
        if (securityToken == null)
        {
            String message = String.format("Failed to generate SecurityToken, probably you should first set " +
                                           "SecurityTokenKey for the user: %s", username);

            return getFailedResponseEntity(HttpStatus.BAD_REQUEST, message, accept);
        }

        Object body = getTokenEntityBody(securityToken, accept);

        return ResponseEntity.ok(body);
    }

    @ApiOperation(value = "Generate authentication token.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_GENERATE_AUTH_TOKEN) })
    @PreAuthorize("authenticated")
    @GetMapping(value = "user/authenticate",
            produces = { MediaType.TEXT_PLAIN_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity authenticate(@RequestParam(name = "expireSeconds", required = false) Integer expireSeconds,
                                       @RequestHeader(HttpHeaders.ACCEPT) String accept)
            throws JoseException
    {
        // We use Security Context from BasicAuth here
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        String authToken = userService.generateAuthenticationToken(username, expireSeconds);

        Object body = getTokenEntityBody(authToken, accept);

        return ResponseEntity.ok(body);
    }

    @ApiOperation(value = "Update custom access model for the user.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_UPDATE_ACCESS_MODEL),
                            @ApiResponse(code = 400, message = FAILED_UPDATE_ACCESS_MODEL),
                            @ApiResponse(code = 404, message = NOT_FOUND_USER) })
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PutMapping(value = "user/{username}/access-model",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = { MediaType.TEXT_PLAIN_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity updateAccessModel(@ApiParam(value = "The name of the user") @PathVariable String username,
                                            @RequestBody @Validated AccessModelForm accessModelForm,
                                            BindingResult bindingResult,
                                            @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_UPDATE_ACCESS_MODEL, bindingResult);
        }

        User user = userService.findByUserName(username);
        if (user == null)
        {
            return getNotFoundResponseEntity(NOT_FOUND_USER, accept);
        }

        userService.updateAccessModel(username, accessModelForm.toDto());

        return getSuccessfulResponseEntity(SUCCESSFUL_UPDATE_USER, accept);
    }

    private Object getUserOutputEntityBody(UserOutput userOutput,
                                           String accept)
    {
        if (MediaType.APPLICATION_JSON_VALUE.equals(accept))
        {
            return userOutput;
        }
        else
        {
            return String.valueOf(userOutput);
        }
    }

    private Object getTokenEntityBody(String token,
                                      String accept)
    {
        if (MediaType.APPLICATION_JSON_VALUE.equals(accept))
        {
            return new TokenEntityBody(token);
        }
        else
        {
            return token;
        }
    }

}
