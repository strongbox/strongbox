package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.resource.ResourceCloser;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.*;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Todorov
 */
@Path("/storages")
public class ArtifactRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactRestlet.class);


    @PUT
    @Path("{storage}/{repository}/{path:.*}")
    public Response upload(@PathParam("storage") String storage,
                           @PathParam("repository") String repository,
                           @PathParam("path") String path,
                           @Context HttpHeaders headers,
                           @Context HttpServletRequest request,
                           InputStream is)
            throws IOException,
                   NoSuchAlgorithmException
    {
        logger.debug("Deploying to " + storage + "/" + repository + "/" + path + "...");

        OutputStream os = null;
        try
        {
            final File file = new File(ConfigurationResourceResolver.getBasedir() + File.separatorChar +
                                       "storages/" + storage + File.separatorChar + repository, path);

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
            throw new WebApplicationException(e, Response.Status.FORBIDDEN);
        }
        finally
        {
            ResourceCloser.close(os, logger);
        }
    }

    @GET
    @Path("{storage}/{repository}/{path:.*}")
    public Response download(@PathParam("storage") String storage,
                             @PathParam("repository") String repository,
                             @PathParam("path") String path,
                             @Context HttpServletRequest request,
                             @Context HttpHeaders headers)
            throws IOException,
                   InstantiationException,
                   IllegalAccessException,
                   ClassNotFoundException
    {
        logger.debug(" repository = " + repository + ", path = " + path);

        if (!ArtifactUtils.isArtifact(path))
        {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // return Response.ok(is).build();
        return Response.ok().build();
    }

    @DELETE
    @Path("{storage}/{repository}/{path:.*}")
    public Response delete(@PathParam("storage") String storage,
                           @PathParam("repository") String repository,
                           @PathParam("path") String path)
            throws IOException
    {
        logger.debug("DELETE: " + path);
        logger.debug(" repository = " + repository + ", path = " + path);

        return Response.ok().build();
    }

}
