package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;

import javax.inject.Inject;
import java.util.LinkedList;

import io.swagger.annotations.*;
import org.jose4j.lang.JoseException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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

    @ApiOperation(value = "Used to create new user",
                  position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The user was created successfully.")})
    @PreAuthorize("hasAuthority('CREATE_USER')")
    @RequestMapping(value = "/user",
                    method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity create(@RequestBody User user)
    {
        user = userService.save(user);
        logger.debug("Created new user " + user);

        return ResponseEntity.ok()
                             .build();
    }


    // ----------------------------------------------------------------------------------------------------------------
    //  Retrieve user

    @ApiOperation(value = "Used to retrieve an user",
                  position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = ""),
                            @ApiResponse(code = 400,
                                         message = "An error occurred.") })
    @PreAuthorize("hasAuthority('VIEW_USER')")
    @RequestMapping(value = "user/{name}",
                    method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    ResponseEntity<User> getUser(@ApiParam(value = "The name of the user",
                                           required = true)
                           @PathVariable String name)
    {
        User user = userService.findByUserName(name);
        if (user == null)
        {
            return ResponseEntity.notFound()
                                 .build();
        }

        return ResponseEntity.ok(user);
    }

    // ----------------------------------------------------------------------------------------------------------------
    //  Retrieve list of users

    @ApiOperation(value = "Used to retrieve an user",
                  position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = ""),
                            @ApiResponse(code = 400,
                                         message = "An error occurred.") })
    @PreAuthorize("hasAuthority('VIEW_USER')")
    @Transactional
    @RequestMapping(value = "/all",
                    method = RequestMethod.GET)
    public
    @ResponseBody
    ResponseEntity getUsers()
    {
        return ResponseEntity.ok(userService.findAll().orElse(new LinkedList<>()));
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Update user

    @ApiOperation(value = "Used to update user",
                  position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The user was updated successfully.")})
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @RequestMapping(value = "user",
                    method = RequestMethod.PUT)
    public
    @ResponseBody
    ResponseEntity update(@RequestBody User userToUpdate)
    {
        userToUpdate = userService.save(userToUpdate);

        return ResponseEntity.ok(userToUpdate);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Delete user by name
    @ApiOperation(value = "Deletes a user from a repository.",
                  position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The user was deleted."),
                            @ApiResponse(code = 400,
                                         message = "Bad request.") })
    @PreAuthorize("hasAuthority('DELETE_USER')")
    @RequestMapping(value = "user/{name}",
                    method = RequestMethod.DELETE)
    public
    @ResponseBody
    ResponseEntity delete(@ApiParam(value = "The name of the user") @PathVariable String name)
            throws Exception
    {
        User user = userService.findByUserName(name);
        if (user == null || user.getObjectId() == null)
        {
            return toError("The specified user does not exist!");
        }

        userService.delete(user.getObjectId());

        return ResponseEntity.ok()
                             .build();
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
