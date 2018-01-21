package org.carlspring.strongbox.controllers.maven;

import org.carlspring.strongbox.controllers.BaseArtifactController;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.repository.RepositoryIndexerNotFoundException;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.IOUtils;
import org.apache.maven.index.context.IndexingContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import static org.carlspring.strongbox.util.IndexContextHelper.getContextId;

/**
 * @author Kate Novik
 * @author carlspring
 */
@RestController
public class MavenIndexController
        extends BaseArtifactController
{

    @Inject
    private ArtifactIndexesService artifactIndexesService;

    @Inject
    private MavenRepositoryFeatures features;


    @ApiOperation(value = "Used to rebuild the indexes in repository or for artifact.",
            position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The indexes were successfully rebuilt!"),
                            @ApiResponse(code = 500,
                                         message = "An error occurred."),
                            @ApiResponse(code = 404,
                                         message = "The specified (storageId/repositoryId/path) does not exist!") })
    @PreAuthorize("hasAuthority('MANAGEMENT_REBUILD_INDEXES')")
    @RequestMapping(value = "/index", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity rebuild(@ApiParam(value = "The storageId", required = true)
                                  @QueryParam("storageId") String storageId,
                                  @ApiParam(value = "The repositoryId", required = true)
                                  @QueryParam("repositoryId") String repositoryId,
                                  @ApiParam(value = "The path")
                                  @QueryParam("path") String path)
            throws IOException
    {
        if (storageId != null && getStorage(storageId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("The specified storageId does not exist!");
        }
        if (repositoryId != null && getRepository(storageId, repositoryId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("The specified repositoryId does not exist!");
        }

        try
        {
            if (storageId != null && repositoryId != null)
            {
                // Rebuild the index for a path under in a repository under a specified storage
                artifactIndexesService.rebuildIndex(storageId, repositoryId, path);
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

    @ApiOperation(value = "Exposes the packed Maven index")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The index has been successfully downloaded!"),
                            @ApiResponse(code = 400, message = "Not a maven repository or indexing not supported."),
                            @ApiResponse(code = 404, message = "The specified (storageId/repositoryId) does not exist!") })
    @GetMapping(value = "/storages/{storageId}/{repositoryId}/.index/" + IndexingContext.INDEX_FILE_PREFIX + ".gz")
    public void downloadIndex(@ApiParam(value = "The storageId", required = true) @PathVariable("storageId") String storageId,
                              @ApiParam(value = "The repositoryId", required = true) @PathVariable("repositoryId") String repositoryId,
                              HttpServletResponse response)
            throws IOException
    {
        final HttpStatus httStatus = preConditionsCheck(storageId, repositoryId, response);
        if (HttpStatus.OK != httStatus)
        {
            return;
        }

        final Path indexPath = packIndex(storageId, repositoryId, response);
        if (indexPath == null)
        {
            return;
        }
        try (final InputStream is = Files.newInputStream(indexPath))
        {
            final int length = IOUtils.copy(is, response.getOutputStream());
            response.setContentType(com.google.common.net.MediaType.GZIP.toString());
            response.setContentLength(length);
            response.flushBuffer();
        }
    }

    @ApiOperation(value = "Exposes the Maven index properties file")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The properties file has been successfully downloaded!"),
                            @ApiResponse(code = 400, message = "Not a maven repository or indexing not supported."),
                            @ApiResponse(code = 404, message = "The specified (storageId/repositoryId) does not exist! or index properties file not found") })
    @GetMapping(value = "/storages/{storageId}/{repositoryId}/.index/" + IndexingContext.INDEX_REMOTE_PROPERTIES_FILE)
    public void downloadProperties(@ApiParam(value = "The storageId", required = true) @PathVariable String storageId,
                                   @ApiParam(value = "The repositoryId", required = true) @PathVariable String repositoryId,
                                   HttpServletResponse response)
            throws IOException
    {
        final HttpStatus httStatus = preConditionsCheck(storageId, repositoryId, response);
        if (HttpStatus.OK != httStatus)
        {
            return;
        }

        Path indexPropertiesPath = features.resolveIndexPath(storageId, repositoryId,
                                                             IndexingContext.INDEX_REMOTE_PROPERTIES_FILE);
        if (!Files.exists(indexPropertiesPath))
        {
            // second chance
            packIndex(storageId, repositoryId, response);
            indexPropertiesPath = features.resolveIndexPath(storageId, repositoryId,
                                                            IndexingContext.INDEX_REMOTE_PROPERTIES_FILE);
        }
        if (!Files.exists(indexPropertiesPath))
        {
            response.sendError(HttpStatus.NOT_FOUND.value(),
                               String.format("Index properties file of repository %s not found.", repositoryId));
            return;
        }

        try (final InputStream is = Files.newInputStream(indexPropertiesPath))
        {
            final int length = IOUtils.copy(is, response.getOutputStream());
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.setContentLength(length);
            response.flushBuffer();
        }
    }

    private HttpStatus preConditionsCheck(String storageId,
                                          String repositoryId,
                                          HttpServletResponse response)
            throws IOException
    {
        if (getStorage(storageId) == null)
        {
            response.sendError(HttpStatus.NOT_FOUND.value(), String.format("Storage %s not found.", storageId));
            return HttpStatus.NOT_FOUND;
        }
        
        Repository repository = getRepository(storageId, repositoryId);
        if (repository == null)
        {
            response.sendError(HttpStatus.NOT_FOUND.value(), String.format("Repository %s not found.", repositoryId));
            return HttpStatus.NOT_FOUND;
        }
        if (!RepositoryLayoutEnum.MAVEN_2.getLayout().equals(repository.getLayout()))
        {
            response.sendError(HttpStatus.BAD_REQUEST.value(),
                               String.format("Repository %s is not a %s repository.", repositoryId,
                                             RepositoryLayoutEnum.MAVEN_2.getLayout()));
            return HttpStatus.BAD_REQUEST;
        }
        
        return HttpStatus.OK;
    }

    private Path packIndex(String storageId,
                           String repositoryId,
                           HttpServletResponse response)
            throws IOException
    {
        try
        {
            return features.pack(storageId, repositoryId);
        }
        catch (RepositoryIndexerNotFoundException ex)
        {
            response.sendError(HttpStatus.BAD_REQUEST.value(),
                               String.format("Repository %s indexer not found. Is indexing enabled for this repository ?",
                                             repositoryId));
            return null;
        }
    }

}
