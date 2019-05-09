package org.carlspring.strongbox.controllers.layout.maven;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.controllers.BaseArtifactController;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.web.LayoutRequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import static org.springframework.http.HttpHeaders.USER_AGENT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * REST API for all artifact-related processes.
 * <p>
 * Thanks to custom URL processing any path variable like '{path:.+}' will be processed as '**'.
 *
 * @author Martin Todorov
 * @author Alex Oreshkevich
 * @author Przemyslaw Fusik
 * @author Sergey Bespalov
 *
 * @see {@linkplain http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html#mvc-config-path-matching}
 */
@RestController
@LayoutRequestMapping(MavenArtifactCoordinates.LAYOUT_NAME)
public class MavenArtifactController
        extends BaseArtifactController
{

    @PreAuthorize("authenticated")
    @RequestMapping(value = "greet", method = RequestMethod.GET)
    public ResponseEntity greet()
    {
        return new ResponseEntity<>("success", HttpStatus.OK);
    }

    @ApiOperation(value = "Used to retrieve an artifact")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 404, message = "Requested path not found."),
                            @ApiResponse(code = 500, message = "Server error."),
                            @ApiResponse(code = 503, message = "Repository currently not in service.")})
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value = { "/{storageId}/{repositoryId}/{path:.+}" }, method = {RequestMethod.GET, RequestMethod.HEAD})
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

        path = correctIndexPathIfNecessary(path);
        RepositoryPath repositoryPath = artifactResolutionService.resolvePath(storageId, repositoryId, path);

        provideArtifactDownloadResponse(request, response, httpHeaders, repositoryPath);
    }

    @ApiOperation(value = "Used to deploy an artifact")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The artifact was deployed successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_DEPLOY')")
    @PutMapping(value = "{storageId}/{repositoryId}/{path:.+}")
    public ResponseEntity upload(@ApiParam(value = "The storageId", required = true)
                                 @PathVariable(name = "storageId") String storageId,
                                 @ApiParam(value = "The repositoryId", required = true)
                                 @PathVariable(name = "repositoryId") String repositoryId,
                                 @PathVariable String path,
                                 HttpServletRequest request)
    {
        try
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, path);

            if (!repositoryPath.getRepository().isInService())
            {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Repository is not in service.");
            }

            artifactManagementService.validateAndStore(repositoryPath, request.getInputStream());

            return ResponseEntity.ok("The artifact was deployed successfully.");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @ApiOperation(value = "Copies a path from one repository to another.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The path was copied successfully."),
                            @ApiResponse(code = 400, message = "Bad request."),
                            @ApiResponse(code = 404, message = "The source/destination storageId/repositoryId/path does not exist!") })
    @PreAuthorize("hasAuthority('ARTIFACTS_COPY')")
    @PostMapping(value = "/copy/{path:.+}")
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
            final Storage srcStorage = getStorage(srcStorageId);
            if (srcStorage == null)
            {
                return ResponseEntity.status(NOT_FOUND)
                                     .body("The source storageId does not exist!");
            }
            final Storage destStorage = getStorage(destStorageId);
            if (destStorage == null)
            {
                return ResponseEntity.status(NOT_FOUND)
                                     .body("The destination storageId does not exist!");
            }
            final Repository srcRepository = srcStorage.getRepository(srcRepositoryId);
            if (srcRepository == null)
            {
                return ResponseEntity.status(NOT_FOUND)
                                     .body("The source repositoryId does not exist!");
            }
            final Repository destRepository = destStorage.getRepository(destRepositoryId);
            if (destRepository == null)
            {
                return ResponseEntity.status(NOT_FOUND)
                                     .body("The destination repositoryId does not exist!");
            }
            final RepositoryPath srcRepositoryPath = repositoryPathResolver.resolve(srcRepository, path);
            if (!Files.exists(srcRepositoryPath))
            {
                return ResponseEntity.status(NOT_FOUND)
                                     .body("The source path does not exist!");
            }

            RepositoryPath srcPath = repositoryPathResolver.resolve(srcRepository, path);
            RepositoryPath destPath = repositoryPathResolver.resolve(destRepository, path);
            
            artifactManagementService.copy(srcPath, destPath);
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

    @ApiOperation(value = "Deletes a path from a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The artifact was deleted."),
                            @ApiResponse(code = 400, message = "Bad request."),
                            @ApiResponse(code = 404, message = "The specified storageId/repositoryId/path does not exist!") })
    @PreAuthorize("hasAuthority('ARTIFACTS_DELETE')")
    @DeleteMapping(value = "/{storageId}/{repositoryId}/{path:.+}")
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
            final RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, path);
            if (!Files.exists(repositoryPath))
            {
                return ResponseEntity.status(NOT_FOUND)
                                     .body("The specified path does not exist!");
            }

            artifactManagementService.delete(repositoryPath, force);
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(e.getMessage());
        }

        return ResponseEntity.ok("The artifact was deleted.");
    }

    private String correctIndexPathIfNecessary(final String requestedPath)
    {
        return MavenIndexPathTransformer.getInstance().apply(requestedPath);
    }

}
