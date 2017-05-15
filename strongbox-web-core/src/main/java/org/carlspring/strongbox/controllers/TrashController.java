package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.ArtifactStorageException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Martin Todorov
 */
@Controller
@RequestMapping("/trash")
@Api(value = "/trash")
public class TrashController
        extends BaseArtifactController
{

    @Inject
    private RepositoryManagementService repositoryManagementService;


    @ApiOperation(value = "Used to delete the trash for a specified repository.",
                  position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The trash for ${storageId}:${repositoryId}' was removed successfully."),
                            @ApiResponse(code = 400,
                                         message = "An error occurred!"),
                            @ApiResponse(code = 404,
                                         message = "The specified (storageId/repositoryId) does not exist!") })
    @PreAuthorize("hasAuthority('MANAGEMENT_DELETE_TRASH')")
    @RequestMapping(value = "{storageId}/{repositoryId}",
                    method = RequestMethod.DELETE,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity delete(@ApiParam(value = "The storageId",
                                           required = true)
                                 @PathVariable String storageId,
                                 @ApiParam(value = "The repositoryId",
                                           required = true)
                                 @PathVariable String repositoryId)
            throws IOException
    {
        if (getStorage(storageId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("The specified storageId does not exist!");
        }
        if (getRepository(storageId, repositoryId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("The specified repositoryId does not exist!");
        }

        try
        {
            repositoryManagementService.deleteTrash(storageId, repositoryId);

            logger.debug("Deleted trash for repository " + repositoryId + ".");
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(e.getMessage());
        }

        return ResponseEntity.ok("The trash for '" + storageId + ":" + repositoryId + "' was removed successfully.");
    }

    @ApiOperation(value = "Used to delete the trash for all repositories.",
                  position = 2)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The trash for all repositories was successfully removed."),
                            @ApiResponse(code = 500,
                                         message = "An error occurred!") })
    @PreAuthorize("hasAuthority('MANAGEMENT_DELETE_ALL_TRASHES')")
    @RequestMapping(method = RequestMethod.DELETE,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity delete()
            throws IOException
    {
        try
        {
            repositoryManagementService.deleteTrash();

            logger.debug("Deleted trash for all repositories.");
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(e.getMessage());
        }

        return ResponseEntity.ok("The trash for all repositories was successfully removed.");
    }

    @ApiOperation(value = "Used to undelete the trash for a path under a specified repository.",
                  position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The trash for '${storageId}:${repositoryId}' was restored successfully."),
                            @ApiResponse(code = 400,
                                         message = "An error occurred!"),
                            @ApiResponse(code = 404,
                                         message = "The specified (storageId/repositoryId/path) does not exist!") })
    @PreAuthorize("hasAuthority('MANAGEMENT_UNDELETE_TRASH')")
    @RequestMapping(value = "{storageId}/{repositoryId}/{path:.+}",
                    method = RequestMethod.POST,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity undelete(@ApiParam(value = "The storageId",
                                             required = true)
                                   @PathVariable String storageId,
                                   @ApiParam(value = "The repositoryId",
                                             required = true)
                                   @PathVariable String repositoryId,
                                   @PathVariable String path,
                                   HttpServletRequest request)
            throws IOException
    {
        if (getStorage(storageId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("The specified storageId does not exist!");
        }
        if (getRepository(storageId, repositoryId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("The specified repositoryId does not exist!");
        }
        if (!new File(getRepository(storageId, repositoryId).getBasedir() + "/.trash", path).exists())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("The specified path does not exist!");
        }

        try
        {
            repositoryManagementService.undelete(storageId, repositoryId, path);

            logger.debug("Undeleted trash for path " + path + " under repository " +
                         storageId + ":" + repositoryId + ".");
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(e.getMessage());
        }

        return ResponseEntity.ok("The trash for '" + storageId + ":" + repositoryId + "' was restored successfully.");
    }

    @ApiOperation(value = "Used to undelete the trash for a specified repository.",
                  position = 4)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The trash for '${storageId}:${repositoryId}' was restored successfully."),
                            @ApiResponse(code = 400,
                                         message = "An error occurred!"),
                            @ApiResponse(code = 404,
                                         message = "The specified (storageId/repositoryId) does not exist!") })
    @PreAuthorize("hasAuthority('MANAGEMENT_UNDELETE_TRASH')")
    @RequestMapping(value = "{storageId}/{repositoryId}",
                    method = RequestMethod.PUT,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity undelete(@ApiParam(value = "The storageId",
                                             required = true)
                                   @PathVariable String storageId,
                                   @ApiParam(value = "The repositoryId",
                                             required = true)
                                   @PathVariable String repositoryId)
            throws Exception
    {
        if (getConfiguration().getStorage(storageId)
                              .getRepository(repositoryId) != null)
        {
            try
            {
                repositoryManagementService.undeleteTrash(storageId, repositoryId);

                logger.debug("Undeleted trash for repository " + repositoryId + ".");
            }
            catch (ArtifactStorageException e)
            {
                if (repositoryManagementService.getStorage(storageId) == null)
                {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                         .body("The specified storageId does not exist!");
                }
                else if (repositoryManagementService.getStorage(storageId)
                                                    .getRepository(repositoryId) == null)
                {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                         .body("The specified repositoryId does not exist!");
                }

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(e.getMessage());
            }

            return ResponseEntity.ok("The trash in '" + storageId + ":" + repositoryId +
                                     "' has been restored successfully.");
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Storage or repository could not be found!");
        }
    }

    @ApiOperation(value = "Used to undelete the trash for all repositories.",
                  position = 5)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The trash for all repositories was successfully restored."),
                            @ApiResponse(code = 400,
                                         message = "An error occurred!") })
    @PreAuthorize("hasAuthority('MANAGEMENT_UNDELETE_ALL_TRASHES')")
    @RequestMapping(method = RequestMethod.POST,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity undelete()
            throws Exception
    {
        try
        {
            repositoryManagementService.undeleteTrash();

            logger.debug("Undeleted trash for all repositories.");
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(e.getMessage());
        }

        return ResponseEntity.ok("The trash for all repositories was successfully restored.");
    }

}
