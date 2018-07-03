package org.carlspring.strongbox.controllers.layout.maven;

import org.carlspring.strongbox.config.MavenIndexerEnabledCondition;
import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import javax.ws.rs.QueryParam;
import java.io.IOException;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import static org.carlspring.strongbox.util.IndexContextHelper.getContextId;

/**
 * @author Kate Novik
 * @author carlspring
 */
@RestController
@Api(value = "/api/maven/index")
@Conditional(MavenIndexerEnabledCondition.class)
public class MavenIndexController
        extends BaseController
{

    private static final Logger logger = LoggerFactory.getLogger(MavenIndexController.class);

    @Inject
    private ArtifactIndexesService artifactIndexesService;
    
    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;


    @ApiOperation(value = "Used to rebuild the indexes in a repository or for artifact.", position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The indexes were successfully rebuilt!"),
                            @ApiResponse(code = 500, message = "An error occurred."),
                            @ApiResponse(code = 404, message = "The specified (storageId/repositoryId/path) does not exist!") })
    @PreAuthorize("hasAuthority('MANAGEMENT_REBUILD_INDEXES')")
    @RequestMapping(path = "/api/maven/index", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity rebuild(@ApiParam(value = "The storageId", required = true)
                                  @QueryParam("storageId") String storageId,
                                  @ApiParam(value = "The repositoryId", required = true)
                                  @QueryParam("repositoryId") String repositoryId,
                                  @ApiParam(value = "The path")
                                  @QueryParam("path") String path)
            throws IOException
    {
        if (storageId != null && getConfiguration().getStorage(storageId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("The specified storageId does not exist!");
        }
        if (repositoryId != null && getConfiguration().getStorage(storageId).getRepository(repositoryId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("The specified repositoryId does not exist!");
        }

        try
        {
            if (storageId != null && repositoryId != null)
            {
                Storage storage = layoutProviderRegistry.getStorage(storageId);
                Repository repository = storage.getRepository(repositoryId);
                
                RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository).resolve(path);
                // Rebuild the index for a path under in a repository under a specified storage
                artifactIndexesService.rebuildIndex(repositoryPath);
            }
            if (storageId != null && repositoryId == null)
            {
                // Rebuild all the indexes in a storage
                artifactIndexesService.rebuildIndexes(storageId);
            }
            if (storageId == null && repositoryId == null)
            {
                // Rebuild all the indexes in all storages
                artifactIndexesService.rebuildIndexes();
            }

            return ResponseEntity.ok("The index for " +
                                     getContextId(storageId, repositoryId, IndexTypeEnum.LOCAL.getType()) +
                                     " was successfully re-built!");
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(e.getMessage());
        }
    }
}
