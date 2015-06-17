package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.security.jaas.authentication.AuthenticationException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.ArtifactStorageException;
import org.carlspring.strongbox.services.ArtifactManagementService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.carlspring.strongbox.util.MessageDigestUtils;
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
    @Path("{storageId}/{repositoryId}/{path:.*}")
    public Response upload(@PathParam("storageId") String storageId,
                           @PathParam("repositoryId") String repositoryId,
                           @PathParam("path") String path,
                           @Context HttpHeaders headers,
                           @Context HttpServletRequest request,
                           InputStream is)
            throws IOException,
                   AuthenticationException,
                   NoSuchAlgorithmException
    {
        handleAuthentication(storageId, repositoryId, path, headers, request);

        try
        {
            artifactManagementService.store(storageId, repositoryId, path, is);

            return Response.ok().build();
        }
        catch (ArtifactStorageException e)
        {
            // TODO: Figure out if this is the correct response type...
            logger.error(e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(), Response.Status.FORBIDDEN);
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
                   ClassNotFoundException,
                   AuthenticationException
    {
        logger.debug(" repository = " + repositoryId + ", path = " + path);

        long offset = 0;

        if (headers != null)
        {
            String contentRange = headers.getRequestHeaders() != null &&
                                  headers.getRequestHeaders().getFirst("Range") != null ?
                                  headers.getRequestHeaders().getFirst("Range") : null;

            if (contentRange != null)
            {
                offset = Long.parseLong(contentRange.substring("bytes=".length(), contentRange.indexOf('-')));

                logger.debug("Received request for partial download, starting at byte " + offset + ".");
            }
        }

        handleAuthentication(storageId, repositoryId, path, headers, request);

        InputStream is;

        try
        {
            is = artifactManagementService.resolve(storageId, repositoryId, path, offset);
        }
        catch (ArtifactResolutionException e)
        {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }

        Response.ResponseBuilder responseBuilder = Response.ok(is);

        // TODO: This is far from optimal and will need to have a content type approach at some point:
        if (ArtifactUtils.isChecksum(path))
        {
            responseBuilder.type(MediaType.TEXT_PLAIN);
        }
        else if (ArtifactUtils.isMetadata(path))
        {
            responseBuilder.type(MediaType.APPLICATION_XML);
        }
        else
        {
            responseBuilder.type(MediaType.APPLICATION_OCTET_STREAM);
        }

        responseBuilder.header("Accept-Ranges", "bytes");

        setHeadersForChecksums(storageId, repositoryId, path, responseBuilder);

        return responseBuilder.build();
    }

    private void setHeadersForChecksums(String storageId,
                                        String repositoryId,
                                        String path,
                                        Response.ResponseBuilder responseBuilder)
            throws IOException
    {
        Storage storage = artifactManagementService.getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        if (!repository.isChecksumHeadersEnabled())
        {
            return;
        }

        InputStream isMd5;
        //noinspection EmptyCatchBlock
        try
        {
            isMd5 = artifactManagementService.resolve(storageId, repositoryId, path + ".md5");
            responseBuilder.header("Checksum-MD5", MessageDigestUtils.readChecksumFile(isMd5));
        }
        catch (IOException e)
        {
            // This can occur if there is no checksum
            logger.warn("There is no MD5 checksum for "  + storageId + "/" + repositoryId + "/" + path);
        }

        InputStream isSha1;
        //noinspection EmptyCatchBlock
        try
        {
            isSha1 = artifactManagementService.resolve(storageId, repositoryId, path + ".sha1");
            responseBuilder.header("Checksum-SHA1", MessageDigestUtils.readChecksumFile(isSha1));
        }
        catch (IOException e)
        {
            // This can occur if there is no checksum
            logger.warn("There is no SHA1 checksum for "  + storageId + "/" + repositoryId + "/" + path);
        }
    }

    @DELETE
    @Path("{storageId}/{repositoryId}/{path:.*}")
    public Response delete(@PathParam("storageId") String storageId,
                           @PathParam("repositoryId") String repositoryId,
                           @PathParam("path") String path,
                           @QueryParam("force") @DefaultValue("false") boolean force)
            throws IOException
    {
        logger.debug("DELETE: " + path);
        logger.debug(storageId + ":" + repositoryId + ": " + path);

        try
        {
            artifactManagementService.delete(storageId, repositoryId, path, force);
        }
        catch (ArtifactStorageException e)
        {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }

        return Response.ok().build();
    }

}
