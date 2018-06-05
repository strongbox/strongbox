package org.carlspring.strongbox.controllers.layout.raw;

import org.carlspring.strongbox.controllers.BaseArtifactController;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * @author carlspring
 */
@RestController
@RequestMapping(path = RawArtifactController.ROOT_CONTEXT, headers = "user-agent=Raw/*")
public class RawArtifactController
        extends BaseArtifactController
{

    private static final Logger logger = LoggerFactory.getLogger(RawArtifactController.class);

    // must be the same as @RequestMapping value on the class definition
    public final static String ROOT_CONTEXT = "/storages";

    @Inject
    private ArtifactManagementService artifactManagementService;
    
    @Inject
    private RepositoryPathResolver repositoryPathResolver;

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
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, path);
            artifactManagementService.validateAndStore(repositoryPath, request.getInputStream());

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
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value = { "{storageId}/{repositoryId}/{path:.+}" }, method = RequestMethod.GET)
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

        RepositoryPath repositoryPath = artifactResolutionService.resolvePath(storageId, repositoryId, path);
        provideArtifactDownloadResponse(request, response, httpHeaders, repositoryPath);
    }

}
