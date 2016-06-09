package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.resource.ResourceCloser;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Todorov
 */
@Path("/storages")
public class DummyArtifactRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(DummyArtifactRestlet.class);

    @PUT
    @Path("{storageId}/{repositoryId}/{path:.*}")
    public Response upload(@PathParam("storageId") String storageId,
                           @PathParam("repositoryId") String repositoryId,
                           @PathParam("path") String path,
                           @Context HttpHeaders headers,
                           @Context HttpServletRequest request,
                           InputStream is)
            throws IOException,
                   NoSuchAlgorithmException
    {
        logger.debug("Deploying to " + storageId + "/" + repositoryId + "/" + path + "...");

        OutputStream os = null;
        try
        {
            final File file = new File(ConfigurationResourceResolver.getVaultDirectory() + File.separatorChar +
                                       "storages/" + storageId + File.separatorChar + repositoryId, path);

            if (!file.getParentFile().exists())
            {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
            }

            logger.debug("Storing to " + file.getAbsolutePath() + "...");

            os = new FileOutputStream(file);

            IOUtils.copy(is, os);

            return Response.ok().build();
        }
        catch (IOException e)
        {
            // TODO: Figure out if this is the correct response type...
            return Response.status(Response.Status.FORBIDDEN).entity("Access denied!").build();
        }
        finally
        {
            ResourceCloser.close(os, logger);
        }
    }

    @GET
    @Path("{storageId}/{repositoryId}/{path:.*}")
    public Response download(@PathParam("storageId") String storageId,
                             @PathParam("repositoryId") String repositoryId,
                             @PathParam("path") String path,
                             @Context HttpServletRequest request,
                             @Context HttpHeaders headers)
            throws IOException,
                   InstantiationException,
                   IllegalAccessException,
                   ClassNotFoundException
    {
        logger.debug(" repositoryId = " + repositoryId + ", path = " + path);

        if (!ArtifactUtils.isArtifact(path))
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // This is not really anything, due to the fact it's only meant to be
        // used for very basic testing, therefore a simple 200 should suffice:
        return Response.ok().build();
    }

    @DELETE
    @Path("{storageId}/{repositoryId}/{path:.*}")
    public Response delete(@PathParam("storageId") String storageId,
                           @PathParam("repositoryId") String repositoryId,
                           @PathParam("path") String path)
            throws IOException
    {
        logger.debug("DELETE: " + path);
        logger.debug(" repository = " + repositoryId + ", path = " + path);

        return Response.ok().build();
    }

}
