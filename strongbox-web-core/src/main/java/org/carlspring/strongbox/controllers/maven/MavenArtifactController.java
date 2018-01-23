package org.carlspring.strongbox.controllers.maven;

import org.carlspring.strongbox.controllers.BaseArtifactController;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.utils.ArtifactControllerHelper;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import static org.carlspring.strongbox.utils.ArtifactControllerHelper.handlePartialDownload;
import static org.carlspring.strongbox.utils.ArtifactControllerHelper.isRangedRequest;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * REST API for all artifact-related processes.
 * <p>
 * Thanks to custom URL processing any path variable like '{path:.+}' will be processed as '**'.
 *
 * @author Alex Oreshkevich
 * @see {@linkplain http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html#mvc-config-path-matching}
 */
@RestController
@RequestMapping(path = MavenArtifactController.ROOT_CONTEXT, headers = "user-agent=Maven/*")
public class MavenArtifactController
        extends BaseArtifactController
{

    private static final Logger logger = LoggerFactory.getLogger(MavenArtifactController.class);

    // must be the same as @RequestMapping value on the class definition
    public final static String ROOT_CONTEXT = "/storages";

    @Inject
    private ArtifactManagementService mavenArtifactManagementService;

    @Inject
    protected ArtifactEventListenerRegistry artifactEventListenerRegistry;


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
    @RequestMapping(value = "{storageId}/{repositoryId}/{path:.+}", method = RequestMethod.PUT)
    public ResponseEntity upload(@ApiParam(value = "The storageId", required = true)
                                 @PathVariable(name = "storageId") String storageId,
                                 @ApiParam(value = "The repositoryId", required = true)
                                 @PathVariable(name = "repositoryId") String repositoryId,
                                 @PathVariable String path,
                                 HttpServletRequest request)
    {
        try
        {
            getArtifactManagementService().validateAndStore(storageId, repositoryId, path, request.getInputStream());

            return ResponseEntity.ok("The artifact was deployed successfully.");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @ApiOperation(value = "Used to retrieve an artifact", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 404, message = "Requested path not found."),
                            @ApiResponse(code = 500, message = "Server Error."),
                            @ApiResponse(code = 503, message = "Repository not in service currently.")})
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value = { "{storageId}/{repositoryId}/{path:.+}" }, method = {RequestMethod.GET, RequestMethod.HEAD})
    public void download(@ApiParam(value = "The storageId", required = true)
                         @PathVariable String storageId,
                         @ApiParam(value = "The repositoryId", required = true)
                         @PathVariable String repositoryId,
                         @RequestHeader HttpHeaders httpHeaders,
                         @PathVariable String path,
                         HttpServletRequest request,
                         HttpServletResponse response)
            throws Exception
    {
        logger.debug("Requested /" + storageId + "/" + repositoryId + "/" + path + ".");

        Storage storage = configurationManager.getConfiguration().getStorage(storageId);
        if (storage == null)
        {
            logger.error("Unable to find storage by ID " + storageId);

            response.sendError(INTERNAL_SERVER_ERROR.value(), "Unable to find storage by ID " + storageId);

            return;
        }

        Repository repository = storage.getRepository(repositoryId);
        if (repository == null)
        {
            logger.error("Unable to find repository by ID " + repositoryId + " for storage " + storageId);

            response.sendError(INTERNAL_SERVER_ERROR.value(),
                               "Unable to find repository by ID " + repositoryId + " for storage " + storageId);
            return;
        }

        if (!repository.isInService())
        {
            logger.error("Repository is not in service...");

            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());

            return;
        }

        if (repository.allowsDirectoryBrowsing() && probeForDirectoryListing(repository, path))
        {
            try
            {
                getDirectoryListing(repository, path, request, response);
            }
            catch (Exception e)
            {
                logger.debug("Unable to generate directory listing for " +
                        "/" + storageId + "/" + repositoryId + "/" + path, e);

                response.setStatus(INTERNAL_SERVER_ERROR.value());
            }

            return;
        }

        
        RepositoryPath resolvedPath = getArtifactManagementService().getPath(storageId, repositoryId, path);
        logger.debug("Resolved path : " + resolvedPath);
        
        ArtifactControllerHelper.provideArtifactHeaders(response, resolvedPath);
        if (!Files.exists(resolvedPath) || request.getMethod().equals(RequestMethod.HEAD.name())) {
            return;
        }

        InputStream is = getArtifactManagementService().resolve(storageId, repositoryId, path);
        if (isRangedRequest(httpHeaders))
        {
            logger.debug("Detecting range request....");
            handlePartialDownload(is, httpHeaders, response);
        }

        artifactEventListenerRegistry.dispatchArtifactDownloadingEvent(storage.getId(), repository.getId(), path);
        copyToResponse(is, response);
        artifactEventListenerRegistry.dispatchArtifactDownloadedEvent(storage.getId(), repository.getId(), path);

        logger.debug("Download succeeded.");
    }

    @ApiOperation(value = "Copies a path from one repository to another.", position = 4)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The path was copied successfully."),
                            @ApiResponse(code = 400, message = "Bad request."),
                            @ApiResponse(code = 404, message = "The source/destination storageId/repositoryId/path does not exist!") })
    @PreAuthorize("hasAuthority('ARTIFACTS_COPY')")
    @RequestMapping(value = "/copy/{path:.+}",
                    method = RequestMethod.POST)
    public ResponseEntity copy(@ApiParam(value = "The source storageId", required = true)
                               @RequestParam(name = "srcStorageId") String srcStorageId,
                               @ApiParam(value = "The source repositoryId", required = true)
                               @RequestParam(name = "srcRepositoryId") String srcRepositoryId,
                               @ApiParam(value = "The destination storageId", required = true)
                               @RequestParam(name = "destStorageId") String destStorageId,
                               @ApiParam(value = "The destination repositoryId", required = true)
                               @RequestParam(name = "destRepositoryId") String destRepositoryId,
                               @PathVariable String path)
    {
        logger.debug("Copying " + path +
                     " from " + srcStorageId + ":" + srcRepositoryId +
                     " to " + destStorageId + ":" + destRepositoryId + "...");

        try
        {
            if (getStorage(srcStorageId) == null)
            {
                return ResponseEntity.status(NOT_FOUND)
                                     .body("The source storageId does not exist!");
            }
            if (getStorage(destStorageId) == null)
            {
                return ResponseEntity.status(NOT_FOUND)
                                     .body("The destination storageId does not exist!");
            }
            if (getStorage(srcStorageId).getRepository(srcRepositoryId) == null)
            {
                return ResponseEntity.status(NOT_FOUND)
                                     .body("The source repositoryId does not exist!");
            }
            if (getStorage(destStorageId).getRepository(destRepositoryId) == null)
            {
                return ResponseEntity.status(NOT_FOUND)
                                     .body("The destination repositoryId does not exist!");
            }
            if (getStorage(srcStorageId) != null &&
                getStorage(srcStorageId).getRepository(srcRepositoryId) != null &&
                !new File(getStorage(srcStorageId).getRepository(srcRepositoryId).getBasedir(), path).exists())
            {
                return ResponseEntity.status(NOT_FOUND)
                                     .body("The source path does not exist!");
            }

            getArtifactManagementService().copy(srcStorageId, srcRepositoryId, destStorageId, destRepositoryId, path);
        }
        catch (ArtifactStorageException e)
        {
            logger.error("Unable to copy artifact due to ArtifactStorageException", e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(e.getMessage());
        }
        catch (Exception e)
        {
            logger.error("Unable to copy artifact", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(e.getMessage());
        }

        return ResponseEntity.ok("The path was copied successfully.");
    }

    @ApiOperation(value = "Deletes a path from a repository.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The artifact was deleted."),
                            @ApiResponse(code = 400, message = "Bad request."),
                            @ApiResponse(code = 404, message = "The specified storageId/repositoryId/path does not exist!") })
    @PreAuthorize("hasAuthority('ARTIFACTS_DELETE')")
    @RequestMapping(value = "{storageId}/{repositoryId}/{path:.+}",
                    method = RequestMethod.DELETE)
    public ResponseEntity delete(@ApiParam(value = "The storageId", required = true)
                                 @PathVariable String storageId,
                                 @ApiParam(value = "The repositoryId", required = true)
                                 @PathVariable String repositoryId,
                                 @ApiParam(value = "Whether to use force delete")
                                 @RequestParam(defaultValue = "false",
                                               name = "force",
                                               required = false) boolean force,
                                 @PathVariable String path)
            throws IOException
    {
        logger.info("Deleting " + storageId + ":" + repositoryId + "/" + path + "...");

        try
        {
            if (getStorage(storageId) == null)
            {
                return ResponseEntity.status(NOT_FOUND)
                                     .body("The specified storageId does not exist!");
            }
            if (getStorage(storageId).getRepository(repositoryId) == null)
            {
                return ResponseEntity.status(NOT_FOUND)
                                     .body("The specified repositoryId does not exist!");
            }
            if (getStorage(storageId) != null &&
                getStorage(storageId).getRepository(repositoryId) != null &&
                !new File(getStorage(storageId).getRepository(repositoryId)
                                               .getBasedir(), path).exists())
            {
                return ResponseEntity.status(NOT_FOUND)
                                     .body("The specified path does not exist!");
            }

            getArtifactManagementService().delete(storageId, repositoryId, path, force);
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(e.getMessage());
        }

        return ResponseEntity.ok("The artifact was deleted.");
    }

    public ArtifactManagementService getArtifactManagementService()
    {
        return mavenArtifactManagementService;
    }

}
