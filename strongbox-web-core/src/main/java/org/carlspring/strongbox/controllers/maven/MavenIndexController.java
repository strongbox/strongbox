package org.carlspring.strongbox.controllers.maven;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.repository.RepositoryIndexerNotFoundException;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import io.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.apache.maven.index.context.IndexingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
// @Api(value = "/api/maven/index")
public class MavenIndexController
        extends BaseController
{

    private static final Logger logger = LoggerFactory.getLogger(MavenIndexController.class);

    @Inject
    private ArtifactIndexesService artifactIndexesService;

    @Inject
    private MavenRepositoryFeatures mavenRepositoryFeatures;

    @Inject
    private ConfigurationManager configurationManager;


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
    @RequestMapping(value = "/storages/{storageId}/{repositoryId}/.index/" + IndexingContext.INDEX_FILE_PREFIX + ".gz",
                    method = RequestMethod.GET)
    public void downloadIndex(@ApiParam(value = "The storageId", required = true) @PathVariable("storageId") String storageId,
                              @ApiParam(value = "The repositoryId", required = true) @PathVariable("repositoryId") String repositoryId,
                              HttpServletResponse response)
            throws IOException
    {
        serveIndexRelatedFile(storageId,
                              repositoryId,
                              IndexingContext.INDEX_FILE_PREFIX + ".gz",
                              com.google.common.net.MediaType.GZIP.toString(),
                              response);
    }

    @ApiOperation(value = "Exposes the Maven index properties file")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The properties file has been successfully downloaded!"),
                            @ApiResponse(code = 400, message = "Not a maven repository or indexing not supported."),
                            @ApiResponse(code = 404, message = "The specified (storageId/repositoryId) does not exist! or index properties file not found") })
    @RequestMapping(value = "/storages/{storageId}/{repositoryId}/.index/" + IndexingContext.INDEX_REMOTE_PROPERTIES_FILE,
                    method = RequestMethod.GET)
    public void downloadProperties(@ApiParam(value = "The storageId", required = true) @PathVariable String storageId,
                                   @ApiParam(value = "The repositoryId", required = true) @PathVariable String repositoryId,
                                   HttpServletResponse response)
            throws IOException
    {
        serveIndexRelatedFile(storageId,
                              repositoryId,
                              IndexingContext.INDEX_REMOTE_PROPERTIES_FILE,
                              MediaType.TEXT_PLAIN_VALUE,
                              response);
    }

    private void serveIndexRelatedFile(String storageId,
                                       String repositoryId,
                                       String path,
                                       String responseContentType,
                                       HttpServletResponse response)
            throws IOException
    {
        final HttpStatus httStatus = preConditionsCheck(storageId, repositoryId, response);
        if (HttpStatus.OK != httStatus)
        {
            return;
        }

        final Path indexPath;
        try
        {
            indexPath = mavenRepositoryFeatures.resolveIndexPath(storageId, repositoryId, path);
        }
        catch (RepositoryIndexerNotFoundException ex)
        {
            response.sendError(HttpStatus.BAD_REQUEST.value(),
                               String.format("Repository %s is not indexable.", repositoryId));
            return;
        }
        if (!Files.exists(indexPath))
        {
            response.sendError(HttpStatus.NOT_FOUND.value(),
                               String.format("Index file does not exist for repository %s.", repositoryId));
            return;
        }
        try (final InputStream is = Files.newInputStream(indexPath))
        {
            final int length = IOUtils.copy(is, response.getOutputStream());
            response.setContentType(responseContentType);
            response.setContentLength(length);
            response.flushBuffer();
        }
    }

    private HttpStatus preConditionsCheck(String storageId,
                                          String repositoryId,
                                          HttpServletResponse response)
            throws IOException
    {
        if (configurationManager.getConfiguration().getStorage(storageId) == null)
        {
            response.sendError(HttpStatus.NOT_FOUND.value(), String.format("Storage %s not found.", storageId));
            return HttpStatus.NOT_FOUND;
        }

        Repository repository = configurationManager.getConfiguration()
                                                    .getStorage(storageId)
                                                    .getRepository(repositoryId);
        if (repository == null)
        {
            response.sendError(HttpStatus.NOT_FOUND.value(), String.format("Repository %s not found.", repositoryId));
            return HttpStatus.NOT_FOUND;
        }
        if (!Maven2LayoutProvider.ALIAS.equals(repository.getLayout()))
        {
            response.sendError(HttpStatus.BAD_REQUEST.value(),
                               String.format("Repository %s is not a %s repository.", repositoryId,
                                             Maven2LayoutProvider.ALIAS));
            return HttpStatus.BAD_REQUEST;
        }

        return HttpStatus.OK;
    }

}
