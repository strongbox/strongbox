package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.users.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

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

    private static final Logger logger = LoggerFactory.getLogger(UserRestlet.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    UserService userService;

    @GET
    @Path("{anyString}")
    @ApiOperation(value = "Used to retrieve an request param", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ROOT')")
    public Response greet(@ApiParam(value = "The param", required = true)
                          @PathParam("anyString") String param)
    {
        logger.debug("UserRestlet -> Say hello to " + param);
        return toResponse("hello, " + param);
    }

    //  Retrieve user
    @GET
    @Path("user/{name}")
    @ApiOperation(value = "Used to retrieve an user", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ROOT')")
    public Response getUser(@Context HttpHeaders headers,
                            @Context HttpServletRequest request,
                            @ApiParam(value = "The name of the user", required = true)
                            @PathParam("name") String name)
    {
        return toResponse(userService.findByUserName(name));
    }

    // test

    private Response toResponse(Object arg)
    {
        try
        {
            return Response.ok(objectMapper.writeValueAsString(arg)).build();
        }
        catch (Exception e)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
