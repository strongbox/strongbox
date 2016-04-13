package org.carlspring.strongbox.rest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.security.jaas.authentication.AuthenticationException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.ArtifactStorageException;
import org.carlspring.strongbox.util.MessageDigestUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import static org.carlspring.commons.http.range.ByteRangeRequestHandler.*;

/**
 * @author Martin Todorov
 */
@Component
@Path("/storages")
public class ArtifactRestlet
        extends BaseArtifactRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactRestlet.class);


    @PUT
    @Path("{storageId}/{repositoryId}/{path:.*}")
    @ApiOperation(value = "Used to deploy an artifact", position = 0)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "An error occurred."),
                            @ApiResponse(code = 200, message = "The artifact deployed successfully.") })
    public Response upload(@ApiParam(value = "The storageIf", required = true)
                           @PathParam("storageId") String storageId,
                           @ApiParam(value = "The repositoryId", required = true)
                           @PathParam("repositoryId") String repositoryId,
                           @ApiParam(value = "The path to the artifact.", required = true)
                           @PathParam("path") String path,
                           @Context HttpHeaders headers,
                           @Context HttpServletRequest request,
                           InputStream is)
            throws IOException,
                   AuthenticationException,
                   NoSuchAlgorithmException
    {
        try
        {
            getArtifactManagementService().store(storageId, repositoryId, path, is);

            return Response.ok().build();
        }
        catch (IOException e)
        {
            // TODO: Figure out if this is the correct response type...
            logger.error(e.getMessage(), e);

            // return Response.status(Response.Status.FORBIDDEN).entity("Access denied!").build();
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("{storageId}/{repositoryId}/{path:.*}")
    @ApiOperation(value = "Used to retrieve an artifact", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "An error occurred."),
                            @ApiResponse(code = 200, message = "The artifact deployed successfully.") })
    public Response download(@ApiParam(value = "The storageId", required = true)
                             @PathParam("storageId") String storageId,
                             @ApiParam(value = "The repositoryId", required = true)
                             @PathParam("repositoryId") String repositoryId,
                             @ApiParam(value = "The path to the artifact", required = true)
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

        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        if (!repository.isInService())
        {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        Response.ResponseBuilder responseBuilder;

        InputStream is;
        try
        {
            if (isRangedRequest(headers))
            {
                is = getArtifactManagementService().resolve(storageId, repositoryId, path);

                responseBuilder = handlePartialDownload((ArtifactInputStream) is, headers);
            }
            else
            {
                is = getArtifactManagementService().resolve(storageId, repositoryId, path);

                responseBuilder = Response.ok(is);
            }
        }
        catch (ArtifactResolutionException e)
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        setMediaTypeHeader(path, responseBuilder);

        responseBuilder.header("Accept-Ranges", "bytes");

        setHeadersForChecksums(storageId, repositoryId, path, responseBuilder);

        return responseBuilder.build();
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

    private void setHeadersForChecksums(String storageId,
                                        String repositoryId,
                                        String path,
                                        Response.ResponseBuilder responseBuilder)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        if (!repository.isChecksumHeadersEnabled())
        {
            return;
        }

        InputStream isMd5;
        //noinspection EmptyCatchBlock
        try
        {
            isMd5 = getArtifactManagementService().resolve(storageId, repositoryId, path + ".md5");
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
            isSha1 = getArtifactManagementService().resolve(storageId, repositoryId, path + ".sha1");
            responseBuilder.header("Checksum-SHA1", MessageDigestUtils.readChecksumFile(isSha1));
        }
        catch (IOException e)
        {
            // This can occur if there is no checksum
            logger.warn("There is no SHA1 checksum for "  + storageId + "/" + repositoryId + "/" + path);
        }
    }

    @POST
    @Path("copy/{path:.*}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Copies a path from one repository to another.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Bad request."),
                            @ApiResponse(code = 404, message = "The source/destination storageId/repositoryId/path does not exist!")})
    public Response copy(@ApiParam(value = "The path", required = true)
                         @PathParam("path") String path,
                         @ApiParam(value = "The source storageId", required = true)
                         @QueryParam("srcStorageId") String srcStorageId,
                         @ApiParam(value = "The source repositoryId", required = true)
                         @QueryParam("srcRepositoryId") String srcRepositoryId,
                         @ApiParam(value = "The destination storageId", required = true)
                         @QueryParam("destStorageId") String destStorageId,
                         @ApiParam(value = "The destination repositoryId", required = true)
                         @QueryParam("destRepositoryId") String destRepositoryId)
            throws IOException
    {
        logger.debug("Copying " + path +
                     " from " + srcStorageId + ":" + srcRepositoryId +
                     " to " + destStorageId + ":" + destRepositoryId + "...");

        try
        {
            if (getStorage(srcStorageId) == null)
            {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("The source storageId does not exist!")
                               .build();
            }
            if (getStorage(destStorageId) == null)
            {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("The destination storageId does not exist!")
                               .build();
            }
            if (getStorage(srcStorageId).getRepository(srcRepositoryId) == null)
            {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("The source repositoryId does not exist!")
                               .build();
            }
            if (getStorage(destStorageId).getRepository(destRepositoryId) == null)
            {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("The destination repositoryId does not exist!")
                               .build();
            }
            if (getStorage(srcStorageId) != null &&
                getStorage(srcStorageId).getRepository(srcRepositoryId) != null &&
                !new File(getStorage(srcStorageId).getRepository(srcRepositoryId).getBasedir(), path).exists())
            {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("The source path does not exist!")
                               .build();
            }

            getArtifactManagementService().copy(srcStorageId, srcRepositoryId, path, destStorageId, destRepositoryId);
        }
        catch (ArtifactStorageException e)
        {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(e.getMessage())
                           .build();
        }

        return Response.ok().build();
    }

    @DELETE
    @Path("{storageId}/{repositoryId}/{path:.*}")
    @ApiOperation(value = "Deletes a path from a repository.", position = 4)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Bad request."),
                            @ApiResponse(code = 404, message = "The specified storageId/repositoryId/path does not exist!")})
    public Response delete(@ApiParam(value = "The storageId", required = true)
                           @PathParam("storageId") String storageId,
                           @ApiParam(value = "The repositoryId", required = true)
                           @PathParam("repositoryId") String repositoryId,
                           @ApiParam(value = "The path to delete", required = true)
                           @PathParam("path") String path,
                           @ApiParam(value = "Whether to use force delete", required = true)
                           @QueryParam("force") @DefaultValue("false") boolean force)
            throws IOException
    {
        logger.debug("DELETE: " + path);
        logger.debug(storageId + ":" + repositoryId + ": " + path);

        try
        {
            if (getStorage(storageId) == null)
            {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("The specified storageId does not exist!")
                               .build();
            }
            if (getStorage(storageId).getRepository(repositoryId) == null)
            {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("The specified repositoryId does not exist!")
                               .build();
            }
            if (getStorage(storageId) != null &&
                getStorage(storageId).getRepository(repositoryId) != null &&
                !new File(getStorage(storageId).getRepository(repositoryId).getBasedir(), path).exists())
            {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("The specified path does not exist!")
                               .build();
            }

            getArtifactManagementService().delete(storageId, repositoryId, path, force);
            deleteMethodFromMetadaInFS(storageId,repositoryId,path);

        }
        catch (ArtifactStorageException e)
        {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(e.getMessage())
                           .build();
        }

        return Response.ok().build();
    }
    
    private void deleteMethodFromMetadaInFS(String storageId, String repositoryId, String metadataPath)
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        final File repoPath = new File(repository.getBasedir());
        
        try
        {
            File artifactFile = new File(repoPath, metadataPath).getCanonicalFile();
            if (!artifactFile.isFile())
            {
                String version = artifactFile.getPath().substring(artifactFile.getPath().lastIndexOf(File.separatorChar) + 1);
                java.nio.file.Path path = Paths.get(artifactFile.getPath().substring(0, artifactFile.getPath().lastIndexOf(File.separatorChar)));

                Metadata metadata = getMetadataManager().readMetadata(path);
                if (metadata != null && metadata.getVersioning() != null)
                {
                    if (metadata.getVersioning().getVersions().contains(version))
                    {
                        metadata.getVersioning().getVersions().remove(version);
                        getMetadataManager().storeMetadata(path, null, metadata, MetadataType.ARTIFACT_ROOT_LEVEL);
                    }
                }
            }
        }
        catch (IOException | XmlPullParserException | NoSuchAlgorithmException e)
        {
            // We won't do anything in this case because it doesn't have an impact to the deletion
        }
    }

}
