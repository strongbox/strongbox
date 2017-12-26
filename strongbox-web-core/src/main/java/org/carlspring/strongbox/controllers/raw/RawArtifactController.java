package org.carlspring.strongbox.controllers.raw;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.controllers.BaseArtifactController;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.storage.ArtifactResolutionException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.utils.ArtifactControllerHelper;

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
import static org.carlspring.strongbox.utils.ArtifactControllerHelper.handlePartialDownload;
import static org.carlspring.strongbox.utils.ArtifactControllerHelper.isRangedRequest;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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
            getArtifactManagementService(storageId, repositoryId).validateAndStore(storageId,
                                                                                   repositoryId,
                                                                                   path,
                                                                                   request.getInputStream());

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

        if (repository.allowsDirectoryBrowsing() && probeForDirectoryListing(repository, path))
        {
            try
            {
                generateDirectoryListing(repository, path, request, response);
            }
            catch (Exception e)
            {
                logger.debug("Unable to generate directory listing for " +
                             "/" + storageId + "/" + repositoryId + "/" + path, e);

                response.setStatus(INTERNAL_SERVER_ERROR.value());
            }

            return;
        }

        ArtifactInputStream is;
        try
        {
            is = (ArtifactInputStream) getArtifactManagementService(storageId, repositoryId).resolve(storageId,
                                                                                                     repositoryId,
                                                                                                     path);
            if (is == null)
            {
                response.setStatus(NOT_FOUND.value());
                return;
            }

            if (isRangedRequest(httpHeaders))
            {
                logger.debug("Detecting range request....");

                handlePartialDownload(is, httpHeaders, response);
            }

            artifactEventListenerRegistry.dispatchArtifactDownloadingEvent(storage.getId(), repository.getId(), path);

            copyToResponse(is, response);

            artifactEventListenerRegistry.dispatchArtifactDownloadedEvent(storage.getId(), repository.getId(), path);
        }
        catch (ArtifactResolutionException | ArtifactTransportException e)
        {
            logger.debug("Unable to find artifact by path " + path, e);

            response.setStatus(NOT_FOUND.value());

            return;
        }

        setMediaTypeHeader(path, response);

        response.setHeader("Accept-Ranges", "bytes");

        ArtifactControllerHelper.setHeadersForChecksums(is, response);

        logger.debug("Download succeeded.");
    }

}
