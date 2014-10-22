package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.security.jaas.authentication.AuthenticationException;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.ArtifactStorageException;
import org.carlspring.strongbox.services.ArtifactManagementService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Martin Todorov
 */
@Component
@Path("/storages")
public class ArtifactRestlet
        extends BaseRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactRestlet.class);

    @Autowired
    private ArtifactManagementService artifactManagementService;


    @PUT
    @Path("{storage}/{repository}/{path:.*}")
    public Response upload(@PathParam("storage") String storage,
                           @PathParam("repository") String repository,
                           @PathParam("path") String path,
                           @Context HttpHeaders headers,
                           @Context HttpServletRequest request,
                           InputStream is)
            throws IOException,
                   AuthenticationException,
                   NoSuchAlgorithmException
    {
        handleAuthentication(storage, repository, path, headers, request);

        try
        {
            artifactManagementService.store(storage, repository, path, is);

            return Response.ok().build();
        }
        catch (ArtifactStorageException e)
        {
            // TODO: Figure out if this is the correct response type...
            throw new WebApplicationException(e, Response.Status.FORBIDDEN);
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
                   ClassNotFoundException,
                   AuthenticationException
    {
        logger.debug(" repository = " + repository + ", path = " + path);

        handleAuthentication(storage, repository, path, headers, request);

        if (!ArtifactUtils.isArtifact(path))
        {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        InputStream is;
        try
        {
            is = artifactManagementService.resolve(storage, repository, path);
        }
        catch (ArtifactResolutionException e)
        {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }

        return Response.ok(is).build();
    }

    @DELETE
    @Path("{storage}/{repository}/{path:.*}")
    public Response delete(@PathParam("storage") String storage,
                           @PathParam("repository") String repository,
                           @PathParam("path") String path,
                           @QueryParam("force") @DefaultValue("false") boolean force)
            throws IOException
    {
        logger.debug("DELETE: " + path);
        logger.debug(" repository = " + repository + ", path = " + path);

        try
        {
            artifactManagementService.delete(storage, repository, path, force);
        }
        catch (ArtifactStorageException e)
        {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }

        return Response.ok().build();
    }

}
