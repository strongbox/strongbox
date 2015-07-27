package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.http.range.ByteRange;
import org.carlspring.strongbox.http.range.ByteRangeHeaderParser;
import org.carlspring.strongbox.io.ArtifactInputStream;
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
import java.util.List;

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
        handleAuthentication(storageId, repositoryId, path, headers, request);

        logger.debug(" repository = " + repositoryId + ", path = " + path);

        Response.ResponseBuilder responseBuilder;

        InputStream is;
        try
        {
            if (isRangedRequest(headers))
            {
                responseBuilder = handlePartialDownload(storageId, repositoryId, path, headers);
            }
            else
            {
                is = artifactManagementService.resolve(storageId, repositoryId, path);

                responseBuilder = Response.ok(is);
            }
        }
        catch (ArtifactResolutionException e)
        {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }

        setMediaTypeHeader(path, responseBuilder);

        responseBuilder.header("Accept-Ranges", "bytes");

        setHeadersForChecksums(storageId, repositoryId, path, responseBuilder);

        return responseBuilder.build();
    }

    private Response.ResponseBuilder handlePartialDownload(String storageId,
                                                           String repositoryId,
                                                           String path,
                                                           HttpHeaders headers)
            throws IOException
    {
        ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headers.getRequestHeaders().getFirst("Range"));
        List<ByteRange> ranges = parser.getRanges();

        if (ranges.size() == 1)
        {
            logger.debug("Received request for a partial download with a single range.");

            return handlePartialDownloadWithSingleRange(storageId, repositoryId, path, ranges.get(0));
        }
        else
        {
            logger.debug("Received request for a partial download with multiple ranges.");

            return handlePartialDownloadWithMultipleRanges(storageId, repositoryId, path, ranges);
        }
    }

    private Response.ResponseBuilder handlePartialDownloadWithSingleRange(String storageId,
                                                                          String repositoryId,
                                                                          String path,
                                                                          ByteRange byteRange)
            throws IOException
    {
        ArtifactInputStream ais = (ArtifactInputStream) artifactManagementService.resolve(storageId, repositoryId, path);
        if (byteRange.getOffset() < ais.getLength())
        {
            // TODO: If OK: Return: 206 Partial Content
            // TODO:     Set headers:
            // TODO:         Accept-Ranges: bytes
            // TODO:         Content-Length: 64656927
            // TODO:         Content-Range: bytes 100-64656926/64656927
            // TODO:         Content-Type: application/jar
            // TODO:         Pragma: no-cache

            ais.setCurrentByteRange(byteRange);
            //noinspection ResultOfMethodCallIgnored
            ais.skip(byteRange.getOffset());

            Response.ResponseBuilder responseBuilder = prepareResponseBuilderForPartialRequest(ais);
            responseBuilder.header("Content-Length", calculatePartialRangeLength(byteRange, ais.getLength()));
            responseBuilder.status(Response.Status.PARTIAL_CONTENT);

            return responseBuilder;
        }
        else
        {
            // TODO: Else: If the byte-range-set is unsatisfiable, the server SHOULD return
            // TODO:       a response with a status of 416 (Requested range not satisfiable).

            return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE);
        }
    }

    private Response.ResponseBuilder handlePartialDownloadWithMultipleRanges(String storageId,
                                                                             String repositoryId,
                                                                             String path,
                                                                             List<ByteRange> byteRanges)
            throws IOException
    {
        // TODO: To be handled as part of SB-367.

        ArtifactInputStream ais = (ArtifactInputStream) artifactManagementService.resolve(storageId, repositoryId, path);
        // TODO: This is not the right check
        if (ais.getCurrentByteRange().getOffset() >= ais.getLength())
        {
            // TODO: If OK: Return: 206 Partial Content
            // TODO:     For each range:
            // TODO:         Set headers:
            // TODO:             Content-Type: application/jar
            // TODO:             Content-Length: 64656927
            // TODO:             Accept-Ranges: bytes
            // TODO:             Content-Range: bytes 100-64656926/64656927
            // TODO:             Pragma: no-cache

            Response.ResponseBuilder responseBuilder = prepareResponseBuilderForPartialRequest(ais);

            // TODO: Add multipart content here

            return responseBuilder;
        }
        else
        {
            // TODO: Else: If the byte-range-set is unsatisfiable, the server SHOULD return
            // TODO:       a response with a status of 416 (Requested range not satisfiable).

            return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE);
        }
    }

    private long calculatePartialRangeLength(ByteRange byteRange, long length)
    {
        if (byteRange.getLimit() > 0 && byteRange.getOffset() > 0)
        {
            logger.debug("Partial content byteRange.getOffset: " + byteRange.getOffset());
            logger.debug("Partial content byteRange.getLimit: " + byteRange.getLimit());
            logger.debug("Partial content length: " + (byteRange.getLimit() - byteRange.getOffset()));

            return byteRange.getLimit() - byteRange.getOffset();
        }
        else if (length > 0 && byteRange.getOffset() > 0 && byteRange.getLimit() == 0)
        {
            logger.debug("Partial content length: " + (length - byteRange.getOffset()));

            return length - byteRange.getOffset();
        }
        else
        {
            return -1;
        }
    }

    private Response.ResponseBuilder prepareResponseBuilderForPartialRequest(ArtifactInputStream ais)
    {
        Response.ResponseBuilder responseBuilder = Response.ok(ais).status(Response.Status.PARTIAL_CONTENT);
        responseBuilder.header("Accept-Ranges", "bytes");
        // responseBuilder.header("Content-Length", ais.getLength());
        responseBuilder.header("Content-Range", "bytes " + ais.getCurrentByteRange().getOffset() + "-" + (ais.getLength() - 1) + "/" + ais.getLength());
        responseBuilder.header("Content-Type", ais.getLength());
        responseBuilder.header("Pragma", "no-cache");

        return responseBuilder;
    }

    private void setMediaTypeHeader(String path, Response.ResponseBuilder responseBuilder)
    {
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
    }

    private boolean isRangedRequest(HttpHeaders headers)
    {
        if (headers == null)
        {
            return false;
        }

        String contentRange = headers.getRequestHeaders() != null &&
                              headers.getRequestHeaders().getFirst("Range") != null ?
                              headers.getRequestHeaders().getFirst("Range") : null;

        return contentRange != null &&
               !contentRange.equals("0/*") && !contentRange.equals("0-") && !contentRange.equals("0");
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
