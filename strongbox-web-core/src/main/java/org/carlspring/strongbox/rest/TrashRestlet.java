package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.resolvers.ArtifactStorageException;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * @author Martin Todorov
 */
@Component
@Path("/trash")
@Api(value = "/trash")
@PreAuthorize("hasAuthority('ROOT')")
public class TrashRestlet
        extends BaseArtifactRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(TrashRestlet.class);

    @Autowired
    private ArtifactManagementService artifactManagementService;


    @DELETE
    @Path("{storageId}/{repositoryId}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Used to delete the trash for a specified repository.", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The trash for ${storageId}:${repositoryId}' was removed successfully."),
                            @ApiResponse(code = 400, message = "An error occurred!"),
                            @ApiResponse(code = 404, message = "The specified (storageId/repositoryId) does not exist!") })
    public Response delete(@ApiParam(value = "The storageId", required = true)
                           @PathParam("storageId") String storageId,
                           @ApiParam(value = "The repositoryId", required = true)
                           @PathParam("repositoryId") String repositoryId)
            throws IOException
    {
        if (getStorage(storageId) == null)
        {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("The specified storageId does not exist!")
                           .build();
        }
        if (getRepository(storageId, repositoryId) == null)
        {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("The specified repositoryId does not exist!")
                           .build();
        }

        try
        {
            artifactManagementService.deleteTrash(storageId, repositoryId);

            logger.debug("Deleted trash for repository " + repositoryId + ".");
        }
        catch (ArtifactStorageException e)
        {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(e.getMessage())
                           .build();
        }

        return Response.ok()
                       .entity("The trash for '" + storageId + ":" + repositoryId + "' was removed successfully.")
                       .build();
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Used to delete the trash for all repositories.", position = 2)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The trash for all repositories was successfully removed."),
                            @ApiResponse(code = 500, message = "An error occurred!") })
    public Response delete()
            throws IOException
    {
        try
        {
            artifactManagementService.deleteTrash();

            logger.debug("Deleted trash for all repositories.");
        }
        catch (ArtifactStorageException e)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

        return Response.ok().entity("The trash for all repositories was successfully removed.").build();
    }

    @POST
    @Path("{storageId}/{repositoryId}/{path:.*}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Used to undelete the trash for a path under a specified repository.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The trash for '${storageId}:${repositoryId}' was restored successfully."),
                            @ApiResponse(code = 400, message = "An error occurred!"),
                            @ApiResponse(code = 404, message = "The specified (storageId/repositoryId/path) does not exist!") })
    public Response undelete(@ApiParam(value = "The storageId", required = true)
                             @PathParam("storageId") String storageId,
                             @ApiParam(value = "The repositoryId", required = true)
                             @PathParam("repositoryId") String repositoryId,
                             @ApiParam(value = "The path to restore", required = true)
                             @PathParam("path") String path)
            throws IOException
    {
        logger.debug("UNDELETE: " + path);
        logger.debug(storageId + ":" + repositoryId + ": " + path);

        if (getStorage(storageId) == null)
        {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("The specified storageId does not exist!")
                           .build();
        }
        if (getRepository(storageId, repositoryId) == null)
        {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("The specified repositoryId does not exist!")
                           .build();
        }
        if (!new File(getRepository(storageId, repositoryId).getBasedir() + "/.trash", path).exists())
        {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("The specified path does not exist!")
                           .build();
        }

        try
        {
            artifactManagementService.undelete(storageId, repositoryId, path);

            logger.debug("Undeleted trash for path " + path + " under repository " + storageId + ":" + repositoryId + ".");
        }
        catch (ArtifactStorageException e)
        {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(e.getMessage())
                           .build();
        }

        return Response.ok()
                       .entity("The trash for '" + storageId + ":" + repositoryId +"' was restored successfully.")
                       .build();
    }

    @POST
    @Path("{storageId}/{repositoryId}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Used to undelete the trash for a specified repository.", position = 4)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The trash for '${storageId}:${repositoryId}' was restored successfully."),
                            @ApiResponse(code = 400, message = "An error occurred!"),
                            @ApiResponse(code = 404, message = "The specified (storageId/repositoryId) does not exist!") })
    public Response undelete(@ApiParam(value = "The storageId", required = true)
                             @PathParam("storageId") String storageId,
                             @ApiParam(value = "The repositoryId", required = true)
                             @PathParam("repositoryId") String repositoryId)
            throws IOException
    {
        if (getConfiguration().getStorage(storageId).getRepository(repositoryId) != null)
        {
            try
            {
                artifactManagementService.undeleteTrash(storageId, repositoryId);

                logger.debug("Undeleted trash for repository " + repositoryId + ".");
            }
            catch (ArtifactStorageException e)
            {
                if (artifactManagementService.getStorage(storageId) == null)
                {
                    return Response.status(Response.Status.NOT_FOUND)
                                   .entity("The specified storageId does not exist!")
                                   .build();
                }
                else if (artifactManagementService.getStorage(storageId).getRepository(repositoryId) == null)
                {
                    return Response.status(Response.Status.NOT_FOUND)
                                   .entity("The specified repositoryId does not exist!")
                                   .build();
                }

                return Response.status(Response.Status.BAD_REQUEST)
                               .entity(e.getMessage())
                               .build();
            }

            return Response.ok()
                           .entity("The trash for '" + storageId + ":" + repositoryId +"' was been restored successfully.")
                           .build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).entity("Storage or repository could not be found!").build();
        }
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Used to undelete the trash for all repositories.", position = 5)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The trash for all repositories was successfully restored."),
                            @ApiResponse(code = 400, message = "An error occurred!") })
    public Response undelete()
            throws IOException
    {
        try
        {
            artifactManagementService.undeleteTrash();

            logger.debug("Undeleted trash for all repositories.");
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);

            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(e.getMessage())
                           .build();
        }

        return Response.ok()
                       .entity("The trash for all repositories was successfully restored.")
                       .build();
    }

}
