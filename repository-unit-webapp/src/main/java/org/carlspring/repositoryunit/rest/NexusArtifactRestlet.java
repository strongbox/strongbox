package org.carlspring.repositoryunit.rest;

import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.repositoryunit.storage.resolvers.ArtifactResolutionException;
import org.carlspring.repositoryunit.storage.resolvers.ArtifactResolutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Martin Todorov
 */
@Path("/nexus/content/repositories")
public class NexusArtifactRestlet
        extends BaseRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(NexusArtifactRestlet.class);


    @PUT
    @Path("/{path:.*}")
    public void upload(@PathParam("path") String path,
                       @Context HttpHeaders headers,
                       byte[] in)
            throws IOException
    {
        // TODO: Implement

        System.out.println("PUT: " + path);
        logger.debug("PUT: " + path);
    }

    @GET
    @Path("/{repository}/{path:.*}")
    public Response download(@PathParam("repository") String repository,
                             @PathParam("path") String artifactPath)
            throws IOException,
                   InstantiationException,
                   IllegalAccessException,
                   ClassNotFoundException
    {
        if (!ArtifactUtils.isArtifact(artifactPath))
        {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        Artifact artifact = ArtifactUtils.convertPathToArtifact(artifactPath);

        InputStream is;
        try
        {
            is = ArtifactResolutionService.getInstance().getInputStreamForArtifact(repository, artifact);
        }
        catch (ArtifactResolutionException e)
        {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return Response.ok(is).build();
    }

    @DELETE
    @Path("/{path:.*}")
    public void delete(@PathParam("path") String path)
            throws IOException
    {
        // TODO: Implement

        System.out.println("DELETE: " + path);
        logger.debug("DELETE: " + path);
    }

}
