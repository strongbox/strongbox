package org.carlspring.strongbox.controllers.users;

import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.userdetails.SpringSecurityUser;

import javax.inject.Inject;
import java.util.Collections;
import java.util.stream.Collectors;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.lang.JoseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
@Api(value = "/users")
public class UserController
        extends BaseController
{

    @Inject
    UserService userService;

    // ----------------------------------------------------------------------------------------------------------------
    // This method exists for testing purpose

    @ApiOperation(value = "Used to retrieve an request param",
            position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200,
            message = ""),
                            @ApiResponse(code = 400,
                                    message = "An error occurred.") })
    @PreAuthorize("authenticated")
    @RequestMapping(value = "/{anyString}",
            method = RequestMethod.GET) // maps to /greet or any other string
    public
    @ResponseBody
    ResponseEntity greet(@PathVariable String anyString,
                         @ApiParam(value = "The param name",
                                 required = true) @RequestParam(value = "name",
                                 required = false) String param)
    {
        logger.debug("UserController -> Say hello to " + param + ". Path variable " + anyString);
        return ResponseEntity.ok("hello, " + param);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Create user

    @ApiOperation(value = "Used to create new user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The user was created successfully.") })
    @PreAuthorize("hasAuthority('CREATE_USER')")
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity create(@RequestBody UserInput userInput)
    {
        userService.save(userInput.asUser());
        return ResponseEntity.ok("The user was created successfully.");
    }


    // ----------------------------------------------------------------------------------------------------------------
    //  Retrieve user

    @ApiOperation(value = "Used to retrieve an user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('VIEW_USER') || #name == principal.username")
    @RequestMapping(value = "user/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity getUser(@ApiParam(value = "The name of the user", required = true) @PathVariable String name)
    {
        User user = userService.findByUserName(name);
        if (user == null)
        {
            return ResponseEntity.notFound()
                                 .build();
        }

        return ResponseEntity.ok(UserOutput.fromUser(user));
    }

    // ----------------------------------------------------------------------------------------------------------------
    //  Retrieve list of users

    @ApiOperation(value = "Used to retrieve an user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('VIEW_USER')")
    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity getUsers()
    {
        return ResponseEntity.ok(
                userService.findAll().orElse(Collections.emptyList()).stream().map(UserOutput::fromUser).collect(
                        Collectors.toList()));
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Update user

    @ApiOperation(value = "Used to update user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The user was updated successfully."),
                            @ApiResponse(code = 400, message = "Bad request was provided to update user") })
    @PreAuthorize("hasAuthority('UPDATE_USER') || #userToUpdate.username == principal.username")
    @RequestMapping(value = "user", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity update(@RequestBody UserInput userToUpdate,
                                 Authentication authentication)
    {
        if (StringUtils.isBlank(userToUpdate.getUsername()))
        {
            return toResponseEntityError("Username not provided", HttpStatus.BAD_REQUEST);
        }
        if (!(authentication.getPrincipal() instanceof SpringSecurityUser))
        {
            return toResponseEntityError(
                    "Unsupported logged user principal type " + authentication.getPrincipal().getClass(),
                    HttpStatus.BAD_REQUEST);
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

        return ResponseEntity.ok("The user was updated successfully.");
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Delete user by name
    @ApiOperation(value = "Deletes a user from a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The user was deleted."),
                            @ApiResponse(code = 400, message = "Bad request.") })
    @PreAuthorize("hasAuthority('DELETE_USER')")
    @RequestMapping(value = "user/{name}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity delete(@ApiParam(value = "The name of the user") @PathVariable String name, Authentication authentication)
            throws Exception
    {
        if (!(authentication.getPrincipal() instanceof SpringSecurityUser))
        {
            return toResponseEntityError(
                    "Unsupported logged user principal type " + authentication.getPrincipal().getClass(),
                    HttpStatus.BAD_REQUEST);
        }
        final SpringSecurityUser loggedUser = (SpringSecurityUser) authentication.getPrincipal();
        if (StringUtils.equals(loggedUser.getUsername(), name))
        {
            return toResponseEntityError("Unable to delete yourself", HttpStatus.BAD_REQUEST);
        }

        User user = userService.findByUserName(name);
        if (user == null || user.getObjectId() == null)
        {
            return toResponseEntityError("The specified user does not exist!", HttpStatus.BAD_REQUEST);
        }

        userService.delete(user.getObjectId());

        return ResponseEntity.ok("The user was deleted.");
    }

    @ApiOperation(value = "Generate new security token for specified user.",
            position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200,
            message = "The security token was generated."),
                            @ApiResponse(code = 500,
                                    message = "An error occurred.") })
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @RequestMapping(value = "user/{userName}/generate-security-token",
            method = RequestMethod.GET)
    public ResponseEntity generateSecurityToken(@ApiParam(value = "The name of the user") @PathVariable String userName)
            throws JoseException

    {
        String result = userService.generateSecurityToken(userName);

        if (result == null)
        {
            return toError(String.format(
                    "Failed to generate SecurityToken, probably you should first set SecurityTokenKey for the user: user-[%s]",
                    userName));
        }

        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Generate authentication token.",
            position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200,
            message = "The authentication token was generated."),
                            @ApiResponse(code = 500,
                                    message = "An error occurred.") })
    @PreAuthorize("authenticated")
    @RequestMapping(value = "user/authenticate",
            method = RequestMethod.GET)
    public ResponseEntity authenticate(@RequestParam(name = "expireSeconds",
            required = false) Integer expireSeconds)
            throws JoseException

    {
        // We use Security Context from BasicAuth here
        String userName = SecurityContextHolder.getContext()
                                               .getAuthentication()
                                               .getName();

        return ResponseEntity.ok(userService.generateAuthenticationToken(userName, expireSeconds));
    }


    @ApiOperation(value = "Update custom access model for the user.",
            position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200,
            message = "The custom access model was updated."),
                            @ApiResponse(code = 403,
                                    message = "Not enough access rights for this operation."),
                            @ApiResponse(code = 500,
                                    message = "An error occurred.") })
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @RequestMapping(value = "user/{userName}/access-model",
            method = RequestMethod.PUT)
    public ResponseEntity<User> updateAccessModel(@ApiParam(value = "The name of the user") @PathVariable
                                                          String userName,
                                                  @RequestBody AccessModel accessModel)
            throws JoseException
    {
        User user = userService.findByUserName(userName);
        if (user == null || user.getObjectId() == null)
        {
            return ResponseEntity.notFound()
                                 .build();   // "The specified user does not exist!"
        }

        user.setAccessModel(accessModel);
        userService.save(user);

        return ResponseEntity.ok(user);
    }

}
