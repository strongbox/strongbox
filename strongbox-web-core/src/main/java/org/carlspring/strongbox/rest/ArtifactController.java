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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.DirectoryFileComparator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import static org.carlspring.strongbox.rest.ByteRangeRequestHandler.handlePartialDownload;
import static org.carlspring.strongbox.rest.ByteRangeRequestHandler.isRangedRequest;

//import static org.carlspring.commons.http.range.ByteRangeRequestHandler.handlePartialDownload;
//import static org.carlspring.commons.http.range.ByteRangeRequestHandler.isRangedRequest;

/**
 * Created by yury on 8/3/16.
 */
@Controller
@RequestMapping("/storages")
public class ArtifactController
        extends BaseArtifactRestlet
{

    private static final Logger logger = LogManager.getLogger(ArtifactController.class.getSimpleName());

    @PreAuthorize("authenticated")
    @RequestMapping(value = "greet", method = RequestMethod.GET)
    public ResponseEntity greet()
    {
        return new ResponseEntity<>("success", HttpStatus.OK);
    }

    @ApiOperation(value = "Used to deploy an artifact", position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The artifact was deployed successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_DEPLOY')")
    @RequestMapping(value = "\"{storageId}/{repositoryId}/{path:.*}\"", method = RequestMethod.PUT)
    public ResponseEntity upload(
                                        @RequestParam(value = "The storageId", required = true)
                                        @PathVariable String storageId,
                                        @RequestParam(value = "The repositoryId", required = true)
                                        @PathVariable String repositoryId,
                                        @RequestParam(value = "The path to the artifact.", required = true)
                                        @PathVariable String path,
                                        InputStream is)
            throws IOException,
                   AuthenticationException,
                   NoSuchAlgorithmException
    {
        try
        {
            getArtifactManagementService().store(storageId, repositoryId, path, is);

            //  return Response.ok().entity("The artifact was deployed successfully.").build();
            return ResponseEntity.ok("The artifact was deployed successfully.");
        }
        catch (IOException e)
        {
            // TODO: Figure out if this is the correct response type...
            logger.error(e.getMessage(), e);

            // return Response.status(Response.Status.FORBIDDEN).entity("Access denied!").build();
            // return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @ApiOperation(value = "Used to retrieve an artifact", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value = "{storageId}/{repositoryId}/{path:.*}", method = RequestMethod.GET)
    public ResponseEntity download(
                                          @RequestParam(value = "The storageId", required = true)
                                          @PathVariable String storageId,
                                          @RequestParam(value = "The repositoryId", required = true)
                                          @PathVariable String repositoryId,
                                          @RequestParam(value = "The path to the artifact.", required = true)
                                          @PathVariable String path,
                                          HttpHeaders headers,
                                          HttpServletRequest request
    )
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
            logger.error("Repository is not in service...");
            //      return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
            return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
        }

        ResponseEntity responseBuilder;

        if (repository.allowsDirectoryBrowsing() && probeForDirectoryListing(repository, path))
        {
            logger.debug("GenerateDirectoryListing...");
            try
            {
                return generateDirectoryListing(repository, path, request);
            }
            catch (Exception e)
            {
                logger.error("Unable to GenerateDirectoryListing", e);
                //  return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
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

                responseBuilder = ResponseEntity.ok(is);
            }
        }
        catch (ArtifactResolutionException | ArtifactTransportException e)
        {
            logger.error("Unable to download", e);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        setMediaTypeHeader(path, responseBuilder);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Accept-Ranges", "bytes");
        responseBuilder = new ResponseEntity(responseHeaders, HttpStatus.OK);

        // responseBuilder.header("Accept-Ranges", "bytes");
        setHeadersForChecksums(storageId, repositoryId, path, responseBuilder);
        logger.debug("Download success. Building response...");

        //  return responseBuilder.build();
        return responseBuilder;
    }

    private void setMediaTypeHeader(String path,
                                    ResponseEntity responseBuilder)
    {
        // TODO: This is far from optimal and will need to have a content type approach at some point:
        if (ArtifactUtils.isChecksum(path))
        {
            //  responseBuilder.type(MediaType.TEXT_PLAIN);
            ResponseEntity temp = responseBuilder;
            HttpHeaders headers = temp.getHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);

            responseBuilder = new ResponseEntity(temp.getBody(), headers, temp.getStatusCode());

        }
        else if (ArtifactUtils.isMetadata(path))
        {
            // responseBuilder.type(MediaType.APPLICATION_XML);
            ResponseEntity temp = responseBuilder;
            HttpHeaders headers = temp.getHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);

            responseBuilder = new ResponseEntity(temp.getBody(), headers, temp.getStatusCode());
        }
        else
        {
            //   responseBuilder.type(MediaType.APPLICATION_OCTET_STREAM);
            ResponseEntity temp = responseBuilder;
            HttpHeaders headers = temp.getHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            responseBuilder = new ResponseEntity(temp.getBody(), headers, temp.getStatusCode());
        }
    }

    private void setHeadersForChecksums(String storageId,
                                        String repositoryId,
                                        String path,
                                        ResponseEntity responseBuilder)
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
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Checksum-MD5", MessageDigestUtils.readChecksumFile(isMd5));
            //   responseBuilder.header("Checksum-MD5", MessageDigestUtils.readChecksumFile(isMd5));
            responseBuilder = new ResponseEntity(responseHeaders, HttpStatus.OK);
        }
        catch (IOException | ArtifactTransportException e)
        {
            // This can occur if there is no checksum
            logger.warn("There is no MD5 checksum for " + storageId + "/" + repositoryId + "/" + path);
        }

        InputStream isSha1;
        //noinspection EmptyCatchBlock
        try
        {
            isSha1 = getArtifactManagementService().resolve(storageId, repositoryId, path + ".sha1");
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Checksum-SHA1", MessageDigestUtils.readChecksumFile(isSha1));
            //   responseBuilder.header("Checksum-SHA1", MessageDigestUtils.readChecksumFile(isSha1));
            responseBuilder = new ResponseEntity(responseHeaders, HttpStatus.OK);
        }
        catch (IOException | ArtifactTransportException e)
        {
            // This can occur if there is no checksum
            logger.warn("There is no SHA1 checksum for " + storageId + "/" + repositoryId + "/" + path);
        }
    }

    private boolean probeForDirectoryListing(Repository repository,
                                             String path)
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

    private ResponseEntity generateDirectoryListing(Repository repository,
                                                    String path,
                                                    HttpServletRequest request)
    {
        path = path.replaceAll("/", Matcher.quoteReplacement(File.separator));

        if (request == null)
        {
            throw new RuntimeException("Unable to retrieve HTTP request from execution context");
        }

        String dir = repository.getBasedir() + File.separator + path;
        String requestUri = request.getRequestURI();

        File file = new File(dir);

        if (file.isDirectory() && !requestUri.endsWith("/"))
        {
            try
            {
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setLocation(new URI(request.getRequestURI() + "/"));
                return new ResponseEntity<>(null, responseHeaders, HttpStatus.TEMPORARY_REDIRECT);
            }
            catch (URISyntaxException e)
            {
                logger.error("Unable to generateDirectoryListing", e);
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        try
        {
            logger.debug(" browsing: " + file.toString());

            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            sb.append("<head>");
            sb.append(
                    "<style>body{font-family: \"Trebuchet MS\", verdana, lucida, arial, helvetica, sans-serif;} table tr {text-align: left;}</style>");
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

                    String lastModified = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(
                            new Date(childFile.lastModified()));

                    sb.append("<tr>");
                    sb.append(
                            "<td><a href='" + URLEncoder.encode(name, "UTF-8") + (childFile.isDirectory() ? "/" : "") +
                            "'>" + name + (childFile.isDirectory() ? "/" : "") + "</a></td>");
                    sb.append("<td>" + lastModified + "</td>");
                    sb.append("<td>" + FileUtils.byteCountToDisplaySize(childFile.length()) + "</td>");
                    sb.append("</tr>");
                }
            }

            sb.append("</table>");
            sb.append("</body>");
            sb.append("</html>");

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.TEXT_HTML);
            return new ResponseEntity<>(sb.toString(), responseHeaders, HttpStatus.FOUND);

        }
        catch (Exception e)
        {
            logger.error(" error accessing requested directory: " + file.getAbsolutePath());
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Copies a path from one repository to another.", position = 4)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The path was copied successfully."),
                            @ApiResponse(code = 400, message = "Bad request."),
                            @ApiResponse(code = 404,
                                         message = "The source/destination storageId/repositoryId/path does not exist!") })
    @PreAuthorize("hasAuthority('ARTIFACTS_COPY')")
    @RequestMapping(produces = MediaType.TEXT_PLAIN_VALUE, value = "copy/{path:.*}", method = RequestMethod.POST)
    public ResponseEntity copy(@RequestParam(value = "The path", name = "The path", required = true)
                               @PathVariable String path,
                               @RequestParam(value = "The source storageId", name = "srcStorageId", required = true)
                                       String srcStorageId,
                               @RequestParam(value = "The source repositoryId", name = "srcRepositoryId",
                                             required = true)
                                       String srcRepositoryId,
                               @RequestParam(value = "The destination storageId", name = "destStorageId",
                                             required = true)
                                       String destStorageId,
                               @RequestParam(value = "The destination repositoryId", name = "destRepositoryId",
                                             required = true)
                                       String destRepositoryId)
            throws IOException
    {
        logger.debug("Copying " + path +
                     " from " + srcStorageId + ":" + srcRepositoryId +
                     " to " + destStorageId + ":" + destRepositoryId + "...");

        try
        {
            if (getStorage(srcStorageId) == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The source storageId does not exist!");
            }
            if (getStorage(destStorageId) == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The destination storageId does not exist!");
            }
            if (getStorage(srcStorageId).getRepository(srcRepositoryId) == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The source repositoryId does not exist!");

            }
            if (getStorage(destStorageId).getRepository(destRepositoryId) == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The destination repositoryId does not exist!");

            }
            if (getStorage(srcStorageId) != null &&
                getStorage(srcStorageId).getRepository(srcRepositoryId) != null &&
                !new File(getStorage(srcStorageId).getRepository(srcRepositoryId).getBasedir(), path).exists())
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The source path does not exist!");

            }

            getArtifactManagementService().copy(srcStorageId, srcRepositoryId, path, destStorageId, destRepositoryId);
        }
        catch (ArtifactStorageException e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        }

        return ResponseEntity.ok("The path was copied successfully.");
    }

    @ApiOperation(value = "Deletes a path from a repository.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The artifact was deleted."),
                            @ApiResponse(code = 400, message = "Bad request."),
                            @ApiResponse(code = 404,
                                         message = "The specified storageId/repositoryId/path does not exist!") })
    @PreAuthorize("hasAuthority('ARTIFACTS_DELETE')")
    @RequestMapping(value = "{storageId}/{repositoryId}/{path:.*}", method = RequestMethod.DELETE)
    public ResponseEntity delete(
                                        @RequestParam(value = "The storageId", name = "storageId", required = true)
                                        @PathVariable String storageId,
                                        @RequestParam(value = "The repositoryId", name = "repositoryId",
                                                      required = true)
                                        @PathVariable String repositoryId,
                                        @RequestParam(value = "The path to delete", name = "path", required = true)
                                        @PathVariable String path,
                                        @RequestParam(value = "Whether to use force delete", defaultValue = "false",
                                                      name = "force", required = true)
                                                boolean force)
            throws IOException
    {
        logger.debug("DELETE: " + path);
        logger.debug(storageId + ":" + repositoryId + ": " + path);

        try
        {
            if (getStorage(storageId) == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The specified storageId does not exist!");

            }
            if (getStorage(storageId).getRepository(repositoryId) == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The specified repositoryId does not exist!");

            }
            if (getStorage(storageId) != null &&
                getStorage(storageId).getRepository(repositoryId) != null &&
                !new File(getStorage(storageId).getRepository(repositoryId).getBasedir(), path).exists())
            {

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The specified path does not exist!");

            }

            getArtifactManagementService().delete(storageId, repositoryId, path, force);
            deleteMethodFromMetadaInFS(storageId, repositoryId, path);

        }
        catch (ArtifactStorageException e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        }

        return ResponseEntity.ok("The artifact was deleted.");
    }

    private void deleteMethodFromMetadaInFS(String storageId,
                                            String repositoryId,
                                            String metadataPath)
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        final File repoPath = new File(repository.getBasedir());

        try
        {
            File artifactFile = new File(repoPath, metadataPath).getCanonicalFile();
            if (!artifactFile.isFile())
            {
                String version = artifactFile.getPath().substring(
                        artifactFile.getPath().lastIndexOf(File.separatorChar) + 1);
                java.nio.file.Path path = Paths.get(
                        artifactFile.getPath().substring(0, artifactFile.getPath().lastIndexOf(File.separatorChar)));

                Metadata metadata = getMetadataManager().readMetadata(path);
                if (metadata != null && metadata.getVersioning() != null
                    && metadata.getVersioning().getVersions().contains(version))
                {
                    metadata.getVersioning().getVersions().remove(version);
                    getMetadataManager().storeMetadata(path, null, metadata, MetadataType.ARTIFACT_ROOT_LEVEL);
                }
            }
        }
        catch (IOException | XmlPullParserException | NoSuchAlgorithmException e)
        {
            // We won't do anything in this case because it doesn't have an impact to the deletion
        }
    }

}
