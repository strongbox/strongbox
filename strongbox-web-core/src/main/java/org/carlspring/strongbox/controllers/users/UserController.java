package org.carlspring.strongbox.controllers.users;

import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.userdetails.SpringSecurityUser;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.lang.JoseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author Pablo Tirado
 */
@Controller
@RequestMapping("/users")
@Api(value = "/users")
public class UserController
        extends BaseController
{

    @Inject
    private UserService userService;


    /**
     * This method exists for testing purposes.
     */
    @ApiOperation(value = "Used to retrieve an request param", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "") })
    @PreAuthorize("authenticated")
    @GetMapping(value = "/{anyString}", produces = { MediaType.TEXT_PLAIN_VALUE,
                                                     MediaType.APPLICATION_JSON_VALUE })
    // maps to /greet or any other string
    @ResponseBody
    public ResponseEntity greet(@PathVariable String anyString,
                                @ApiParam(value = "The param name", required = true)
                                @RequestParam(value = "name", required = false) String param,
                                @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        logger.debug("UserController -> Say hello to {}. Path variable: {}", param, anyString);
        return ResponseEntity.ok(getResponseEntityBody("hello, " + param, accept));
    }

    @ApiOperation(value = "Used to create new user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The user was created successfully."),
                            @ApiResponse(code = 409, message = "A user with this username already exists! Please enter another username..") })
    @PreAuthorize("hasAuthority('CREATE_USER')")
    @PostMapping(value = "/user", produces = { MediaType.TEXT_PLAIN_VALUE,
                                               MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public ResponseEntity create(@RequestBody UserInput userInput,
                                 @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        User user = userService.findByUserName(userInput.asUser().getUsername());
        if (user != null)
        {
            String message = "A user with this username already exists! Please enter another username.";
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body(getResponseEntityBody(message, accept));
        }

        userService.save(userInput.asUser());

        return ResponseEntity.ok(getResponseEntityBody("The user was created successfully.", accept));
    }

    @ApiOperation(value = "Used to retrieve an user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred."),
                            @ApiResponse(code = 404, message = "The specified user does not exist!.") })
    @PreAuthorize("hasAuthority('VIEW_USER') || #name == principal.username")
    @GetMapping(value = "user/{name}", produces = { MediaType.TEXT_PLAIN_VALUE,
                                                    MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public ResponseEntity getUser(@ApiParam(value = "The name of the user", required = true) @PathVariable String name,
                                  @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        User user = userService.findByUserName(name);
        if (user == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(getResponseEntityBody("The specified user does not exist!", accept));
        }

        return ResponseEntity.ok(getUserOutputEntityBody(UserOutput.fromUser(user), accept));
    }

    @ApiOperation(value = "Used to retrieve an user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('VIEW_USER')")
    @GetMapping(value = "/all", produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public ResponseEntity getUsers()
    {
        List<UserOutput> users = userService.findAll()
                                            .orElse(Collections.emptyList())
                                            .stream()
                                            .map(UserOutput::fromUser).collect(Collectors.toList());

        return getJSONListResponseEntityBody("users", users);
    }

    @ApiOperation(value = "Used to update user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The user was updated successfully."),
                            @ApiResponse(code = 400, message = "Could not update user.") })
    @PreAuthorize("hasAuthority('UPDATE_USER') || #userToUpdate.username == principal.username")
    @PutMapping(value = "user", produces = { MediaType.TEXT_PLAIN_VALUE,
                                             MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public ResponseEntity update(@RequestBody UserInput userToUpdate,
                                 Authentication authentication,
                                 @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        if (StringUtils.isBlank(userToUpdate.getUsername()))
        {
            String message = "Username not provided.";

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody(message, accept));
        }
        if (!(authentication.getPrincipal() instanceof SpringSecurityUser))
        {
            String message = "Unsupported logged user principal type " + authentication.getPrincipal().getClass();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody(message, accept));
        }

        User user = userToUpdate.asUser();
        final SpringSecurityUser loggedUser = (SpringSecurityUser) authentication.getPrincipal();
        if (StringUtils.equals(loggedUser.getUsername(), user.getUsername()))
        {
            userService.updatePassword(user);
        }
        else
        {
            userService.updateByUsername(user);
        }

        return ResponseEntity.ok(getResponseEntityBody("The user was updated successfully.", accept));
    }

    @ApiOperation(value = "Deletes a user from a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The user was deleted."),
                            @ApiResponse(code = 400, message = "Could not delete a user."),
                            @ApiResponse(code = 404, message = "The specified user does not exist!") })
    @PreAuthorize("hasAuthority('DELETE_USER')")
    @DeleteMapping(value = "user/{name}", produces = { MediaType.TEXT_PLAIN_VALUE,
                                                       MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public ResponseEntity delete(@ApiParam(value = "The name of the user") @PathVariable String name,
                                 Authentication authentication,
                                 @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        if (!(authentication.getPrincipal() instanceof SpringSecurityUser))
        {
            String message = "Unsupported logged user principal type " + authentication.getPrincipal().getClass();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody(message, accept));
        }
        final SpringSecurityUser loggedUser = (SpringSecurityUser) authentication.getPrincipal();
        if (StringUtils.equals(loggedUser.getUsername(), name))
        {
            String message = "Unable to delete yourself";

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody(message, accept));
        }

        User user = userService.findByUserName(name);
        if (user == null || user.getObjectId() == null)
        {
            String message = "The specified user does not exist!";

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(getResponseEntityBody(message, accept));
        }

        userService.delete(user.getObjectId());

        return ResponseEntity.ok(getResponseEntityBody("The user was deleted.", accept));
    }

    @ApiOperation(value = "Generate new security token for specified user.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The security token was generated."),
                            @ApiResponse(code = 400, message = "Could not generate new security token.") })
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @GetMapping(value = "user/{username}/generate-security-token", produces = { MediaType.TEXT_PLAIN_VALUE,
                                                                                MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity generateSecurityToken(@ApiParam(value = "The name of the user") @PathVariable String username,
                                                @RequestHeader(HttpHeaders.ACCEPT) String accept)
            throws JoseException
    {
        String securityToken = userService.generateSecurityToken(username);

        if (securityToken == null)
        {
            String message = String.format("Failed to generate SecurityToken, probably you should first set" +
                                           " SecurityTokenKey for the user: user-[%s]", username);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody(message, accept));
        }

        return ResponseEntity.ok(getTokenEntityBody(securityToken, accept));
    }

    @ApiOperation(value = "Generate authentication token.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The authentication token was generated."),
                            @ApiResponse(code = 400, message = "Could not generate authentication token..") })
    @PreAuthorize("authenticated")
    @GetMapping(value = "user/authenticate", produces = { MediaType.TEXT_PLAIN_VALUE,
                                                          MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity authenticate(@RequestParam(name = "expireSeconds", required = false) Integer expireSeconds,
                                       @RequestHeader(HttpHeaders.ACCEPT) String accept)
            throws JoseException
    {
        // We use Security Context from BasicAuth here
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        String authToken = userService.generateAuthenticationToken(username, expireSeconds);

        return ResponseEntity.ok(getTokenEntityBody(authToken, accept));
    }


    @ApiOperation(value = "Update custom access model for the user.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The custom access model was updated."),
                            @ApiResponse(code = 403, message = "Not enough access rights for this operation."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PutMapping(value = "user/{username}/access-model", produces = { MediaType.TEXT_PLAIN_VALUE,
                                                                     MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity updateAccessModel(@ApiParam(value = "The name of the user") @PathVariable String userName,
                                            @RequestBody AccessModel accessModel,
                                            @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        User user = userService.findByUserName(userName);
        if (user == null || user.getObjectId() == null)
        {
            String message = "The specified user does not exist!";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(getResponseEntityBody(message, accept));
        }

        user.setAccessModel(accessModel);
        userService.save(user);

        return ResponseEntity.ok(getUserEntityBody(user, accept));
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

    private Object getUserEntityBody(User user,
                                     String accept)
    {
        if (MediaType.APPLICATION_JSON_VALUE.equals(accept))
        {
            return user;
        }
        else
        {
            return String.valueOf(user);
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
