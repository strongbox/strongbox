package org.carlspring.strongbox.controller;

import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;

import javax.inject.Inject;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import static org.carlspring.strongbox.util.IndexContextHelper.getContextId;

/**
 * @author Kate Novik.
 */
@RestController
@RequestMapping("/index")
@Api(value = "/index")
public class ArtifactIndexesController
        extends BaseArtifactController
{

    @Inject
    private ArtifactIndexesService artifactIndexesService;


    @ApiOperation(value = "Used to rebuild the indexes in repository or for artifact.", position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The indexes was successfully rebuilt!"),
                            @ApiResponse(code = 500, message = "An error occurred."),
                            @ApiResponse(code = 404, message = "The specified (storageId/repositoryId/path) does not exist!") })
    @PreAuthorize("hasAuthority('MANAGEMENT_REBUILD_INDEXES')")
    @RequestMapping(value = "{storageId}/{repositoryId}/{path:.+}",
                    method = RequestMethod.POST,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity rebuild(@ApiParam(value = "The storageId", required = true)
                                  @PathVariable String storageId,
                                  @ApiParam(value = "The repositoryId", required = true)
                                  @PathVariable String repositoryId,
                                  @ApiParam(value = "The artifactPath")
                                  @PathVariable String path)
            throws IOException
    {
        if (getStorage(storageId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The specified storageId does not exist!");
        }
        if (getRepository(storageId, repositoryId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The specified repositoryId does not exist!");
        }
        if (path != null && !new File(getRepository(storageId, repositoryId).getBasedir(), path).exists())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The specified path does not exist!");
        }
        try
        {
            artifactIndexesService.rebuildIndex(storageId, repositoryId, path);

            return ResponseEntity.ok("The index for " +
                                     getContextId(storageId, repositoryId, IndexTypeEnum.LOCAL.getType()) +
                                     " was successfully re-built!");
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @ApiOperation(value = "Used to rebuild the indexes in storage.", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The indexes was successfully rebuilt!"),
                            @ApiResponse(code = 500, message = "An error occurred."),
                            @ApiResponse(code = 404, message = "The specified storageId does not exist!") })
    @PreAuthorize("hasAuthority('MANAGEMENT_REBUILD_INDEXES')")
    @RequestMapping(value = "{storageId}",
                    method = RequestMethod.POST,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity rebuild(@ApiParam(value = "The storageId", required = true)
                                  @PathVariable String storageId)
            throws IOException
    {
        if (getStorage(storageId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The specified storageId does not exist!");
        }

        try
        {
            artifactIndexesService.rebuildIndexes(storageId);

            return ResponseEntity.ok("The indexes for " + storageId + ":*:local were successfully re-built!");
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @ApiOperation(value = "Used to rebuild the indexes in storage.", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The indexes was successfully rebuilt!"),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('MANAGEMENT_REBUILD_INDEXES')")
    @RequestMapping(method = RequestMethod.POST,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity rebuild()
            throws IOException
    {
        try
        {
            artifactIndexesService.rebuildIndexes();

            return ResponseEntity.ok("The indexes for all repositories were successfully re-built!");
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
