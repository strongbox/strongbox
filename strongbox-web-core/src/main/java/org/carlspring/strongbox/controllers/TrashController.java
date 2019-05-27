package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;

import io.swagger.annotations.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author Martin Todorov
 * @author Pablo Tirado
 */
@Controller
@RequestMapping("/api/trash")
@Api(value = "/api/trash")
public class TrashController
        extends BaseController
{

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;


    @ApiOperation(value = "Used to delete the trash for a specified repository.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The trash for ${storageId}:${repositoryId}' was removed successfully."),
                            @ApiResponse(code = 400,
                                         message = "Could not delete the trash for a specified storageId/repositoryId."),
                            @ApiResponse(code = 404,
                                         message = "The specified (storageId/repositoryId) does not exist!") })
    @PreAuthorize("hasAuthority('MANAGEMENT_DELETE_TRASH')")
    @DeleteMapping(value = "{storageId}/{repositoryId}",
                   produces = { MediaType.TEXT_PLAIN_VALUE,
                                MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity delete(@ApiParam(value = "The storageId", required = true)
                                 @PathVariable String storageId,
                                 @ApiParam(value = "The repositoryId", required = true)
                                 @PathVariable String repositoryId,
                                 @RequestHeader(HttpHeaders.ACCEPT) String accept)
            throws IOException
    {
        if (getStorage(storageId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(getResponseEntityBody("The specified storageId does not exist!", accept));
        }
        if (getRepository(storageId, repositoryId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(getResponseEntityBody("The specified repositoryId does not exist!", accept));
        }

        try
        {
            repositoryManagementService.deleteTrash(storageId, repositoryId);

            logger.debug("Deleted trash for repository {}.", repositoryId);
        }
        catch (ArtifactStorageException e)
        {
            String message = "Could not delete the trash for a specified storageId/repositoryId.";
            logger.error(message, e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody(message, accept));
        }

        String message = "The trash for '" + storageId + ":" + repositoryId + "' was removed successfully.";
        return ResponseEntity.ok(getResponseEntityBody(message, accept));
    }

    @ApiOperation(value = "Used to delete the trash for all repositories.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The trash for all repositories was successfully removed."),
                            @ApiResponse(code = 400,
                                         message = "Could not delete the trash for all repositories.") })
    @PreAuthorize("hasAuthority('MANAGEMENT_DELETE_ALL_TRASHES')")
    @DeleteMapping(produces = { MediaType.TEXT_PLAIN_VALUE,
                                MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity delete(@RequestHeader(HttpHeaders.ACCEPT) String accept)
            throws IOException
    {
        try
        {
            repositoryManagementService.deleteTrash();

            logger.debug("Deleted trash for all repositories.");
        }
        catch (ArtifactStorageException e)
        {
            String message = "Could not delete the trash for all repositories.";
            logger.error(message, e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody(message, accept));
        }

        String message = "The trash for all repositories was successfully removed.";
        return ResponseEntity.ok(getResponseEntityBody(message, accept));
    }

    @ApiOperation(value = "Used to undelete the trash for a path under a specified repository.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The trash for '${storageId}:${repositoryId}' was restored successfully."),
                            @ApiResponse(code = 400,
                                         message = "Could not restore the trash for the specified repository."),
                            @ApiResponse(code = 404,
                                         message = "The specified (storageId/repositoryId/path) does not exist!") })
    @PreAuthorize("hasAuthority('MANAGEMENT_UNDELETE_TRASH')")
    @PostMapping("{storageId}/{repositoryId}/{path:.+}")
    public ResponseEntity undelete(@ApiParam(value = "The storageId", required = true)
                                   @PathVariable String storageId,
                                   @ApiParam(value = "The repositoryId", required = true)
                                   @PathVariable String repositoryId,
                                   @PathVariable String path,
                                   @RequestHeader(HttpHeaders.ACCEPT) String accept)
            throws IOException
    {
        if (getStorage(storageId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(getResponseEntityBody("The specified storageId does not exist!", accept));
        }
        Repository repository = getRepository(storageId, repositoryId);
        if (repository == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(getResponseEntityBody("The specified repositoryId does not exist!", accept));
        }
        
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, path);
        RepositoryPath trashPath = RepositoryFiles.trash(repositoryPath);
        if (!Files.exists(trashPath))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(getResponseEntityBody("The specified path does not exist!", accept));
        }

        try
        {
            repositoryManagementService.undelete(repositoryPath);

            logger.debug("Undeleted trash for path {} under repository {}:{}.", path, storageId, repositoryId);
        }
        catch (ArtifactStorageException e)
        {
            String message = "Could not restore the trash for the specified repository.";

            logger.error(message, e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody(message, accept));
        }

        String message = "The trash for '" + storageId + ":" + repositoryId + "' was restored successfully.";
        return ResponseEntity.ok(getResponseEntityBody(message, accept));
    }

    @ApiOperation(value = "Used to undelete the trash for a specified repository.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The trash for '${storageId}:${repositoryId}' was restored successfully."),
                            @ApiResponse(code = 400,
                                         message = "Could not restore the trash for a specified repository."),
                            @ApiResponse(code = 404,
                                         message = "The specified (storageId/repositoryId) does not exist!") })
    @PreAuthorize("hasAuthority('MANAGEMENT_UNDELETE_TRASH')")
    @PutMapping(value = "{storageId}/{repositoryId}",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity undelete(@ApiParam(value = "The storageId", required = true)
                                   @PathVariable String storageId,
                                   @ApiParam(value = "The repositoryId", required = true)
                                   @PathVariable String repositoryId,
                                   @RequestHeader(HttpHeaders.ACCEPT) String accept)
            throws Exception
    {
        if (getConfiguration().getStorage(storageId)
                              .getRepository(repositoryId) != null)
        {
            try
            {
                repositoryManagementService.undeleteTrash(storageId, repositoryId);

                logger.debug("Undeleted trash for repository {}.", repositoryId);
            }
            catch (ArtifactStorageException e)
            {
                if (repositoryManagementService.getStorage(storageId) == null)
                {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                         .body(getResponseEntityBody("The specified storageId does not exist!", accept));
                }
                else if (repositoryManagementService.getStorage(storageId)
                                                    .getRepository(repositoryId) == null)
                {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                         .body(getResponseEntityBody("The specified repositoryId does not exist!", accept));
                }

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(getResponseEntityBody("Could not restore the trash for a specified repository.", accept));
            }

            return ResponseEntity.ok(getResponseEntityBody("The trash in '" + storageId + ":" + repositoryId + "' " +
                                                           "has been restored successfully.", accept));
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(getResponseEntityBody("Storage or repository could not be found!", accept));
        }
    }

    @ApiOperation(value = "Used to undelete the trash for all repositories.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The trash for all repositories was successfully restored."),
                            @ApiResponse(code = 400,
                                         message = "Could not restore the trash for all repositories.") })
    @PreAuthorize("hasAuthority('MANAGEMENT_UNDELETE_ALL_TRASHES')")
    @PostMapping(produces = { MediaType.TEXT_PLAIN_VALUE,
                              MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity undelete(@RequestHeader(HttpHeaders.ACCEPT) String accept)
            throws Exception
    {
        try
        {
            repositoryManagementService.undeleteTrash();

            logger.debug("Undeleted trash for all repositories.");
        }
        catch (ArtifactStorageException e)
        {
            String message = "Could not restore the trash for all repositories.";

            logger.error(message, e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody(message, accept));
        }

        return ResponseEntity.ok(getResponseEntityBody("The trash for all repositories was successfully restored.",
                                                       accept));
    }

}
