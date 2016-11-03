package org.carlspring.strongbox.controller;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.security.exceptions.AuthenticationException;
import org.carlspring.strongbox.storage.ArtifactResolutionException;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.util.MessageDigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;

import com.google.common.io.ByteStreams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.DirectoryFileComparator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import static org.carlspring.strongbox.utils.ByteRangeRequestHandler.handlePartialDownload;
import static org.carlspring.strongbox.utils.ByteRangeRequestHandler.isRangedRequest;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * test
 */
@RestController
@RequestMapping("/storages")
public class ArtifactController
        extends BaseArtifactController
{

    private static final Logger logger = LogManager.getLogger(ArtifactController.class.getName());

    // must be the same as @RequestMapping value on the class definition
    public final static String ROOT_CONTEXT = "/storages";

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
    @RequestMapping(value = "{storageId}/{repositoryId}/**", method = RequestMethod.PUT)
    public ResponseEntity upload(@ApiParam(value = "The storageId", required = true)
                                 @PathVariable(name = "storageId") String storageId,
                                 @ApiParam(value = "The repositoryId", required = true)
                                 @PathVariable(name = "repositoryId") String repositoryId,
                                 HttpServletRequest request)
            throws IOException,
                   AuthenticationException,
                   NoSuchAlgorithmException,
                   JAXBException,
                   ProviderImplementationException
    {
        try
        {
            String path = convertRequestToPath(ROOT_CONTEXT, request, storageId, repositoryId);
            getArtifactManagementService().store(storageId, repositoryId, path, request.getInputStream());

            return ResponseEntity.ok("The artifact was deployed successfully.");
        }
        catch (IOException e)
        {
            // TODO: Figure out if this is the correct response type...
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @ApiOperation(value = "Used to retrieve an artifact", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value = "{storageId}/{repositoryId}/**", method = RequestMethod.GET,
            consumes = MediaType.TEXT_PLAIN_VALUE)
    public void download(@ApiParam(value = "The storageId", required = true)
                         @PathVariable String storageId,
                         @ApiParam(value = "The repositoryId", required = true)
                         @PathVariable String repositoryId,
                         @RequestHeader HttpHeaders httpHeaders,
                         HttpServletRequest request,
                         HttpServletResponse response)
            throws Exception
    {
        String path = convertRequestToPath(ROOT_CONTEXT, request, storageId, repositoryId);

        logger.debug(" repository = " + repositoryId + ", path = " + path);
        Storage storage = configurationManager.getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        if (!repository.isInService())
        {
            logger.error("Repository is not in service...");
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            return;
        }

        if (repository.allowsDirectoryBrowsing() && probeForDirectoryListing(repository, path))
        {
            logger.debug("GenerateDirectoryListing...");
            try
            {
                generateDirectoryListing(repository, path, request, response);
            }
            catch (Exception e)
            {
                logger.error("Unable to GenerateDirectoryListing", e);
                response.setStatus(INTERNAL_SERVER_ERROR.value());

            }
            return;
        }

        InputStream is;
        try
        {
            is = getArtifactManagementService().resolve(storageId, repositoryId, path);

            if (isRangedRequest(httpHeaders))
            {
                logger.debug("Detecting range request....");
                copyToResponse(handlePartialDownload((ArtifactInputStream) is, httpHeaders, response), response);
            }
            else
            {
                copyToResponse(is, response);
            }
        }
        catch (ArtifactResolutionException | ArtifactTransportException e)
        {
            logger.error("Unable to download artifact: " + e.getLocalizedMessage(), e);
            response.setStatus(NOT_FOUND.value());
            return;
        }

        setMediaTypeHeader(path, response);

        response.setHeader("Accept-Ranges", "bytes");

        setHeadersForChecksums(storageId, repositoryId, path, response);

        logger.info("Download success.");
    }

    private void copyToResponse(InputStream inputStream,
                                HttpServletResponse response)
            throws Exception
    {
        try
        {
            long totalBytes = ByteStreams.copy(new BufferedInputStream(inputStream), response.getOutputStream());
            response.setHeader("Content-Length", totalBytes + "");
            response.flushBuffer();
            inputStream.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable copy to response", e);
        }
    }

    private void setMediaTypeHeader(String path,
                                    HttpServletResponse response)
    {
        // TODO: This is far from optimal and will need to have a content type approach at some point:
        if (ArtifactUtils.isChecksum(path))
        {
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        }
        else if (ArtifactUtils.isMetadata(path))
        {
            response.setContentType(MediaType.APPLICATION_XML_VALUE);
        }
        else
        {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        }
    }

    private void setHeadersForChecksums(String storageId,
                                        String repositoryId,
                                        String path,
                                        HttpServletResponse response)
            throws IOException, JAXBException, NoSuchAlgorithmException, ProviderImplementationException
    {
        Storage storage = configurationManager.getConfiguration().getStorage(storageId);
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
            response.setHeader("Checksum-MD5", MessageDigestUtils.readChecksumFile(isMd5));
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
            response.setHeader("Checksum-SHA1", MessageDigestUtils.readChecksumFile(isSha1));
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

    private void generateDirectoryListing(Repository repository,
                                          String path,
                                          HttpServletRequest request,
                                          HttpServletResponse response)
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
            response.setLocale(new Locale(request.getRequestURI() + "/"));
            response.setStatus(HttpStatus.TEMPORARY_REDIRECT.value());

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
                    sb.append("<td><a href='" + URLEncoder.encode(name, "UTF-8") + (childFile.isDirectory() ?
                                                                                    "/" : "") + "'>" + name +
                              (childFile.isDirectory() ? "/" : "") + "</a></td>");
                    sb.append("<td>" + lastModified + "</td>");
                    sb.append("<td>" + FileUtils.byteCountToDisplaySize(childFile.length()) + "</td>");
                    sb.append("</tr>");
                }
            }

            sb.append("</table>");
            sb.append("</body>");
            sb.append("</html>");

            response.setContentType("text/html;charset=UTF-8");
            response.setStatus(HttpStatus.FOUND.value());
            response.getWriter().write(sb.toString());
            response.getWriter().flush();
            response.getWriter().close();

        }
        catch (Exception e)
        {
            logger.error(" error accessing requested directory: " + file.getAbsolutePath());
            response.setStatus(404);
        }
    }

    @ApiOperation(value = "Copies a path from one repository to another.", position = 4)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The path was copied successfully."),
                            @ApiResponse(code = 400, message = "Bad request."),
                            @ApiResponse(code = 404, message = "The source/destination storageId/repositoryId/path does not exist!") })
    @PreAuthorize("hasAuthority('ARTIFACTS_COPY')")
    @RequestMapping(produces = MediaType.TEXT_PLAIN_VALUE, value = "/copy/**", method = RequestMethod.POST)
    public ResponseEntity copy(@ApiParam(value = "The source storageId", required = true)
                               @RequestParam(name = "srcStorageId") String srcStorageId,
                               @ApiParam(value = "The source repositoryId", required = true)
                               @RequestParam(name = "srcRepositoryId") String srcRepositoryId,
                               @ApiParam(value = "The destination storageId", required = true)
                               @RequestParam(name = "destStorageId") String destStorageId,
                               @ApiParam(value = "The destination repositoryId", required = true)
                               @RequestParam(name = "destRepositoryId") String destRepositoryId,
                               HttpServletRequest request)

            throws IOException, JAXBException
    {
        String path = convertRequestToPath(ROOT_CONTEXT, request, "copy");

        logger.debug("Copying " + path +
                     " from " + srcStorageId + ":" + srcRepositoryId +
                     " to " + destStorageId + ":" + destRepositoryId + "...");

        try
        {
            if (getStorage(srcStorageId) == null)
            {
                return ResponseEntity.status(NOT_FOUND).body("The source storageId does not exist!");
            }
            if (getStorage(destStorageId) == null)
            {
                return ResponseEntity.status(NOT_FOUND).body("The destination storageId does not exist!");
            }
            if (getStorage(srcStorageId).getRepository(srcRepositoryId) == null)
            {
                return ResponseEntity.status(NOT_FOUND).body("The source repositoryId does not exist!");

            }
            if (getStorage(destStorageId).getRepository(destRepositoryId) == null)
            {
                return ResponseEntity.status(NOT_FOUND).body("The destination repositoryId does not exist!");

            }
            if (getStorage(srcStorageId) != null &&
                getStorage(srcStorageId).getRepository(srcRepositoryId) != null &&
                !new File(getStorage(srcStorageId).getRepository(srcRepositoryId).getBasedir(), path).exists())
            {
                return ResponseEntity.status(NOT_FOUND).body("The source path does not exist!");

            }

            getArtifactManagementService().copy(srcStorageId, srcRepositoryId, path, destStorageId, destRepositoryId);
        }
        catch (ArtifactStorageException e)
        {
            logger.error("Unable to copy artifact due to ArtifactStorageException", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (Exception e)
        {
            logger.error("Unable to copy artifact", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.ok("The path was copied successfully.");
    }

    @ApiOperation(value = "Deletes a path from a repository.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The artifact was deleted."),
                            @ApiResponse(code = 400, message = "Bad request."),
                            @ApiResponse(code = 404,
                                    message = "The specified storageId/repositoryId/path does not exist!") })
    @PreAuthorize("hasAuthority('ARTIFACTS_DELETE')")
    @RequestMapping(value = "{storageId}/{repositoryId}/**", method = RequestMethod.DELETE)
    public ResponseEntity delete(@ApiParam(value = "The storageId", required = true)
                                 @PathVariable String storageId,
                                 @ApiParam(value = "The repositoryId", required = true)
                                 @PathVariable String repositoryId,
                                 @ApiParam(value = "Whether to use force delete")
                                 @RequestParam(defaultValue = "false", name = "force") boolean force,
                                 HttpServletRequest request)
            throws IOException, JAXBException
    {
        String path = convertRequestToPath(ROOT_CONTEXT, request, storageId, repositoryId);

        logger.info("[delete] path " + path);
        logger.debug(storageId + ":" + repositoryId + ": " + path);

        try
        {
            if (getStorage(storageId) == null)
            {
                return ResponseEntity.status(NOT_FOUND).body("The specified storageId does not exist!");

            }
            if (getStorage(storageId).getRepository(repositoryId) == null)
            {
                return ResponseEntity.status(NOT_FOUND).body("The specified repositoryId does not exist!");

            }
            if (getStorage(storageId) != null &&
                getStorage(storageId).getRepository(repositoryId) != null &&
                !new File(getStorage(storageId).getRepository(repositoryId).getBasedir(), path).exists())
            {

                return ResponseEntity.status(NOT_FOUND).body("The specified path does not exist!");

            }

            getArtifactManagementService().delete(storageId, repositoryId, path, force);
            deleteMetadataFromFS(storageId, repositoryId, path);
        }
        catch (ArtifactStorageException e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        }

        return ResponseEntity.ok("The artifact was deleted.");
    }

    private void deleteMetadataFromFS(String storageId,
                                      String repositoryId,
                                      String metadataPath)
    {
        Storage storage = configurationManager.getConfiguration().getStorage(storageId);
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

                Metadata metadata = getMavenMetadataManager().readMetadata(path);
                if (metadata != null && metadata.getVersioning() != null
                    && metadata.getVersioning().getVersions().contains(version))
                {
                    metadata.getVersioning().getVersions().remove(version);
                    getMavenMetadataManager().storeMetadata(path, null, metadata, MetadataType.ARTIFACT_ROOT_LEVEL);
                }
            }
        }
        catch (IOException | XmlPullParserException | NoSuchAlgorithmException e)
        {
            // We won't do anything in this case because it doesn't have an impact to the deletion
        }
    }

}
