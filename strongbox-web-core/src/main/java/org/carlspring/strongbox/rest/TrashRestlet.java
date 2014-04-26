package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionService;

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
    private ArtifactResolutionService artifactResolutionService;


    @DELETE
    @Path("{repository}")
    public Response delete(@PathParam("repository") String repository)
            throws IOException
    {
        logger.debug("Deleting trash for repository " + repository);

        try
        {
            artifactResolutionService.deleteTrash(repository);
        }
        catch (ArtifactResolutionException e)
        {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }

        return Response.ok().build();
    }

    @DELETE
    public Response delete()
            throws IOException
    {
        logger.debug("Deleting trash for all repositories...");

        try
        {
            artifactResolutionService.deleteTrash();
        }
        catch (ArtifactResolutionException e)
        {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }

        return Response.ok().build();
    }

}
