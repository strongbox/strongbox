package org.carlspring.strongbox.controller;

import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.ArtifactStorageException;

import java.io.File;
import java.io.IOException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kate Novik.
 */
@RestController
@RequestMapping("/index")
@Api(value = "/index")
public class ArtifactIndexesController
        extends BaseArtifactController
{

    @Autowired
    private ArtifactIndexesService artifactIndexesService;

    @ApiOperation(value = "Used to rebuild the indexes in repository or for artifact.",
                  position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The indexes was successfully rebuilt!"),
                            @ApiResponse(code = 500,
                                         message = "An error occurred."),
                            @ApiResponse(code = 404,
                                         message = "The specified (storageId/repositoryId/path) does not exist!") })
    @PreAuthorize("hasAuthority('MANAGEMENT_REBUILD_INDEXES')")
    @RequestMapping(value = "{storageId}/{repositoryId}/{path:.+}",
                    method = RequestMethod.POST,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity rebuild(@ApiParam(value = "The storageId",
                                            required = true)
                                  @PathVariable String storageId,
                                  @ApiParam(value = "The repositoryId",
                                            required = true)
                                  @PathVariable String repositoryId,
                                  @ApiParam(value = "The artifactPath")
                                  @PathVariable String path)
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
        if (path != null && !new File(getRepository(storageId, repositoryId).getBasedir(), path).exists())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("The specified path does not exist!");
        }
        try
        {
            artifactIndexesService.rebuildIndexes(storageId, repositoryId, path);

            return ResponseEntity.ok("The Indexes was successfully rebuilt!");
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(e.getMessage());
        }
    }

    @ApiOperation(value = "Used to rebuild the indexes in storage.",
                  position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The indexes was successfully rebuilt!"),
                            @ApiResponse(code = 500,
                                         message = "An error occurred."),
                            @ApiResponse(code = 404,
                                         message = "The specified storageId does not exist!") })
    @PreAuthorize("hasAuthority('MANAGEMENT_REBUILD_INDEXES')")
    @RequestMapping(value = "{storageId}",
                    method = RequestMethod.POST,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity rebuild(@ApiParam(value = "The storageId",
                                            required = true)
                                  @PathVariable String storageId)
            throws IOException
    {
        if (getStorage(storageId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("The specified storageId does not exist!");
        }
        try
        {
            artifactIndexesService.rebuildIndexes(storageId);

            return ResponseEntity.ok("The Indexes was successfully rebuilt!");
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(e.getMessage());
        }
    }

    @ApiOperation(value = "Used to rebuild the indexes in storage.",
                  position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The indexes was successfully rebuilt!"),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @PreAuthorize("hasAuthority('MANAGEMENT_REBUILD_INDEXES')")
    @RequestMapping(method = RequestMethod.POST,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity rebuild()
            throws IOException
    {
        try
        {
            artifactIndexesService.rebuildIndexes();

            return ResponseEntity.ok("The Indexes was successfully rebuilt!");
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(e.getMessage());
        }
    }
}
