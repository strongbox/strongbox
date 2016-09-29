package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Defines REST API for managing users.
 *
 * @author Alex Oreshkevich
 */
@Component
@Path("/users")
@Api(value = "/users")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public class UserRestlet
        extends BaseArtifactRestlet
{

    @Autowired
    UserService userService;

    @Autowired
    OObjectDatabaseTx databaseTx;

    @Autowired
    CacheManager cacheManager;

    // ----------------------------------------------------------------------------------------------------------------
    // This method exists for testing purpose
    @GET
    @Path("{anyString}")
    @ApiOperation(value = "Used to retrieve an request param", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("isAuthenticated()")
    public synchronized Response greet(@ApiParam(value = "The param", required = true)
                                       @PathParam("anyString") String param)
    {
        logger.debug("UserRestlet -> Say hello to " + param);
        return toResponse("hello, " + param);
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Create user
    @POST
    @Path("user")
    @ApiOperation(value = "Used to create new user", position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The user was created successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CREATE_USER')")
    public synchronized Response create(String userJson)
    {
        databaseTx.activateOnCurrentThread();
        User user = databaseTx.detach(userService.save(read(userJson, User.class)), true);

        logger.debug("Create new user " + user);

        cacheManager.getCache("users").put(user.getUsername(), user);

        return Response.ok().build();
    }


    // ----------------------------------------------------------------------------------------------------------------
    //  Retrieve user
    @GET
    @Path("user/{name}")
    @ApiOperation(value = "Used to retrieve an user", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "User was retrieved."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('VIEW_USER')")
    public synchronized Response getUser(@Context HttpHeaders headers,
                                         @Context HttpServletRequest request,
                                         @ApiParam(value = "The name of the user", required = true)
                                         @PathParam("name") String name)
    {
        return toResponse(userService.findByUserName(name));
    }


    // ----------------------------------------------------------------------------------------------------------------
    //  Retrieve list of users
    @GET
    @Path("/all")
    @ApiOperation(value = "Used to retrieve an user", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "User was retrieved."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('VIEW_USER') or isAnonymous()")
    @Transactional
    public synchronized Response getUsers()
    {
        Optional<List<User>> possibleUsers = userService.findAll();
        if (possibleUsers.isPresent())
        {
            // TODO Due to internal error in spring-data-orientdb
            // com.orientechnologies.orient.client.remote.OStorageRemote cannot be cast to
            // com.orientechnologies.orient.core.storage.impl.local.paginated.OLocalPaginatedStorage
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
    @PUT
    @Path("user")
    @ApiOperation(value = "Used to update user", position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The user was updated successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public synchronized Response update(String userJson)
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
    @DELETE
    @Path("user/{name}")
    @ApiOperation(value = "Deletes a user by name.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The user was deleted."),
                            @ApiResponse(code = 400, message = "Bad request.")
    })
    @PreAuthorize("hasAuthority('DELETE_USER')")
    public Response delete(@ApiParam(value = "The name of the user", required = true)
                           @PathParam("name") String name)
            throws Exception
    {
        User user = userService.findByUserName(name);
        if (user == null || user.getId() == null)
        {
            return toError("The specified user does not exist!");
        }

        userService.delete(user.getId());

        return Response.ok().build();
    }
}
