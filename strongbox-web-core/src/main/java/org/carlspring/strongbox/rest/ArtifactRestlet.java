package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.security.jaas.authentication.AuthenticationException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.ArtifactStorageException;
import org.carlspring.strongbox.util.MessageDigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.DirectoryFileComparator;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import static org.carlspring.commons.http.range.ByteRangeRequestHandler.handlePartialDownload;
import static org.carlspring.commons.http.range.ByteRangeRequestHandler.isRangedRequest;

/**
 * @author Martin Todorov
 */
@Component
@Path("/storages")
@Api(value = "/storages")
public class ArtifactRestlet
        extends BaseArtifactRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactRestlet.class);

    @GET
    @Path("greet")
    @PreAuthorize("hasAuthority('ROOT')")
    public Response greet(@Context HttpHeaders headers, @Context HttpServletRequest request){
        return Response.status(Response.Status.OK).entity("success").build();
    }

    @PUT
    @Path("{storageId}/{repositoryId}/{path:.*}")
    @ApiOperation(value = "Used to deploy an artifact", position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The artifact was deployed successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    public Response upload(@ApiParam(value = "The storageId", required = true)
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

            return Response.ok().entity("The artifact was deployed successfully.").build();
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
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ROOT')")
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

        if (repository.allowsDirectoryBrowsing() && probeForDirectoryListing(repository, path))
        {
            return generateDirectoryListing(repository, path, request);
        }

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
        catch (ArtifactResolutionException | ArtifactTransportException e)
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
        catch (IOException | ArtifactTransportException e)
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
        catch (IOException | ArtifactTransportException e)
        {
            // This can occur if there is no checksum
            logger.warn("There is no SHA1 checksum for "  + storageId + "/" + repositoryId + "/" + path);
        }
    }

    private boolean probeForDirectoryListing(Repository repository, String path)
    {
        String filePath = path.replaceAll("/", Matcher.quoteReplacement(File.separator));

        String dir = repository.getBasedir() + File.separator + filePath;

        File file = new File(dir);

        // Do not allow .index and .trash directories (or any other directory starting with ".") to be browsable.
        // NB: Files will still be downloadable.
        if (!file.isHidden() && !path.startsWith(".") && !path.contains("/."))
        {
            if (file.exists() && file.isDirectory())
            {
                return true;
            }

            file = new File(dir + File.separator);

            return file.exists() && file.isDirectory();
        }
        else
        {
            return false;
        }
    }

    private Response generateDirectoryListing(Repository repository, String path, HttpServletRequest request)
    {
        path = path.replaceAll("/", Matcher.quoteReplacement(File.separator));

        String dir = repository.getBasedir() + File.separator + path;
        String uri = request.getRequestURI();

        File file = new File(dir);

        if (file.isDirectory() && !uri.endsWith("/"))
        {
            try
            {
                return Response.status(Response.Status.TEMPORARY_REDIRECT)
                               .location(new URI(request.getRequestURI() + "/"))
                               .build();
            }
            catch (URISyntaxException e)
            {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .build();
            }
        }

        try
        {
            logger.debug(" browsing: " + file.toString());

            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            sb.append("<head>");
            sb.append("<style>body{font-family: \"Trebuchet MS\", verdana, lucida, arial, helvetica, sans-serif;} table tr {text-align: left;}</style>");
            sb.append("<title>Index of " + request.getRequestURI() + "</title>");
            sb.append("</head>");
            sb.append("<body>");
            sb.append("<h1>Index of " + request.getRequestURI() + "</h1>");
            sb.append("<table cellspacing=\"10\">");
            sb.append("<tr>");
            sb.append("<th>Name</th>");
            sb.append("<th>Last modified</th>");
            sb.append("<th>Size</th>");
            sb.append("</tr>");
            sb.append("<tr>");
            sb.append("<td colspan=3><a href='..'>..</a></td>");
            sb.append("</tr>");

            File[] childFiles = file.listFiles();
            if (childFiles != null)
            {
                Arrays.sort(childFiles, DirectoryFileComparator.DIRECTORY_COMPARATOR);

                for (File childFile : childFiles)
                {
                    String name = childFile.getName();

                    if (name.startsWith(".") || childFile.isHidden())
                    {
                        continue;
                    }

                    String lastModified = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(new Date(childFile.lastModified()));

                    sb.append("<tr>");
                    sb.append("<td><a href='" + URLEncoder.encode(name, "UTF-8") + (childFile.isDirectory() ? "/" : "") + "'>" + name + (childFile.isDirectory() ? "/" : "") + "</a></td>");
                    sb.append("<td>" + lastModified + "</td>");
                    sb.append("<td>" + FileUtils.byteCountToDisplaySize(childFile.length()) + "</td>");
                    sb.append("</tr>");
                }
            }

            sb.append("</table>");
            sb.append("</body>");
            sb.append("</html>");

            return Response.ok()
                           .status(Response.Status.FOUND)
                           .type(MediaType.TEXT_HTML)
                           .entity(sb.toString())
                           .build();
        }
        catch (Exception e)
        {
            logger.warn(" error accessing requested directory: " + file.getAbsolutePath());
            return Response.status(Response.Status.NOT_FOUND)
                           .build();
        }
    }

    @POST
    @Path("copy/{path:.*}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Copies a path from one repository to another.", position = 4)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The path was copied successfully."),
                            @ApiResponse(code = 400, message = "Bad request."),
                            @ApiResponse(code = 404, message = "The source/destination storageId/repositoryId/path does not exist!")})
    @PreAuthorize("hasAuthority('ROOT')")
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

        return Response.ok()
                       .entity("The path was copied successfully.")
                       .build();
    }

    @DELETE
    @Path("{storageId}/{repositoryId}/{path:.*}")
    @ApiOperation(value = "Deletes a path from a repository.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The artifact was deleted."),
                            @ApiResponse(code = 400, message = "Bad request."),
                            @ApiResponse(code = 404, message = "The specified storageId/repositoryId/path does not exist!")})
    @PreAuthorize("hasAuthority('ROOT')")
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

        return Response.ok()
                       .entity("The artifact was deleted.")
                       .build();
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
