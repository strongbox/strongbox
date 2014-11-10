package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.storage.resolvers.ArtifactStorageException;
import org.carlspring.strongbox.services.ArtifactManagementService;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Martin Todorov
 */
@Component
@Path("/trash")
public class TrashRestlet
        extends BaseRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(TrashRestlet.class);

    @Autowired
    private ArtifactManagementService artifactManagementService;


    @DELETE
    @Path("{storageId}/{repositoryId}")
    public Response delete(@PathParam("storageId") String storageId,
                           @PathParam("repositoryId") String repositoryId)
            throws IOException
    {
        if (getDataCenter().getStorage(storageId).getRepository(repositoryId) != null)
        {
            try
            {
                artifactManagementService.deleteTrash(storageId, repositoryId);

                logger.debug("Deleted trash for repository " + repositoryId + ".");
            }
            catch (ArtifactStorageException e)
            {
                throw new WebApplicationException(e, Response.Status.NOT_FOUND);
            }

            return Response.ok().build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).entity("Storage or repository could not be found!").build();
        }
    }

    @DELETE
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
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return Response.ok().build();
    }

}
