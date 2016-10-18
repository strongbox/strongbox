package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController
{

    private static final Logger logger = LoggerFactory.getLogger(UserRestlet.class);

    @Inject
    UserService userService;

    @Inject
    OObjectDatabaseTx databaseTx;

    @Inject
    CacheManager cacheManager;

    @Inject
    ObjectMapper objectMapper;

    // ----------------------------------------------------------------------------------------------------------------
    // This method exists for testing purpose

    @ApiOperation(value = "Used to retrieve an request param", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("authenticated")
    @RequestMapping(value = "/{anyString}", method = RequestMethod.GET)  // maps to /greet or any other string
    public
    @ResponseBody
    ResponseEntity greet(@PathVariable String anyString,
                                      @RequestParam(value = "name", required = false) String param)
    {
        logger.debug("UserController -> Say hello to " + param + ". Path variable " + anyString);
        return toResponse("hello, " + param);
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Create user

    @ApiOperation(value = "Used to create new user", position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The artifact was deployed successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CREATE_USER')")
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public
    @ResponseBody
    synchronized ResponseEntity create(@RequestParam(value = "juser", required = false) String userJson)
    {
        databaseTx.activateOnCurrentThread();
        User user = databaseTx.detach(userService.save(read(userJson, User.class)), true);

        logger.debug("Create new user " + user);

        cacheManager.getCache("users").put(user.getUsername(), user);

        return ResponseEntity.ok().build();
    }


    // ----------------------------------------------------------------------------------------------------------------
    //  Retrieve user

    @ApiOperation(value = "Used to retrieve an user", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('VIEW_USER')")
    @RequestMapping(value = "user/{name}", method = RequestMethod.GET)
    public
    @ResponseBody
    ResponseEntity getUser(@PathVariable String name)
    {
        return toResponse(userService.findByUserName(name));
    }

    // ----------------------------------------------------------------------------------------------------------------
    //  Retrieve list of users

    @ApiOperation(value = "Used to retrieve an user", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('VIEW_USER')")
    @Transactional
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public
    @ResponseBody
    synchronized ResponseEntity getUsers()
    {
        Optional<List<User>> possibleUsers = userService.findAll();
        if (possibleUsers.isPresent())
        {
            // TODO Due to internal error in spring-data-orientdb
            // com.orientechnologies.orient.client.remote.OStorageRemote cannot be cast to com.orientechnologies.orient.core.storage.impl.local.paginated.OLocalPaginatedStorage
            List<User> users = new LinkedList<>();
            possibleUsers.get().forEach(user -> users.add(databaseTx.detach(user, true)));

            return toResponse(users);
        }
        else
        {
            return toError("Unable to get list of users. See UserServiceImpl for details");
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Update user

    @ApiOperation(value = "Used to create new user", position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The artifact was deployed successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @RequestMapping(value = "user", method = RequestMethod.PUT)
    public
    @ResponseBody
    synchronized ResponseEntity update(@RequestParam(value = "juser", required = false) String userJson)
    {

        User user = read(userJson, User.class);
        String id = user.getId();

        if (id == null || !userService.findOne(id).isPresent())
        {
            return toError("Unable to update non-existing user with id " + id);
        }

        logger.debug("Update user by id " + id);

        // do save
        user = databaseTx.detach(userService.save(read(userJson, User.class)), true);
        cacheManager.getCache("users").put(user.getUsername(), user);

        return toResponse(user);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Delete user by name
    @ApiOperation(value = "Deletes a user from a repository.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The user was deleted."),
                            @ApiResponse(code = 400, message = "Bad request.")
    })
    @PreAuthorize("hasAuthority('DELETE_USER')")
    @RequestMapping(value = "user/{name}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    ResponseEntity delete(@RequestParam(value = "The name of the user", required = true) @PathVariable String name)
            throws Exception
    {
        User user = userService.findByUserName(name);
        if (user == null || user.getId() == null)
        {
            return toError("The specified user does not exist!");
        }

        userService.delete(user.getId());

        return ResponseEntity.ok().build();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Common-purpose methods

    private synchronized <T> T read(String json,
                                    Class<T> type)
    {
        try
        {
            return objectMapper.readValue(json, type);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private synchronized ResponseEntity toResponse(Object arg)
    {
        try
        {
            return ResponseEntity.ok(objectMapper.writeValueAsString(arg));
        }
        catch (Exception e)
        {
            return toError(e);
        }
    }

    private synchronized ResponseEntity toError(String message)
    {
        return toError(new RuntimeException(message));
    }

    private synchronized ResponseEntity toError(Throwable cause)
    {
        logger.error(cause.getMessage(), cause);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(cause.getMessage());
    }
}