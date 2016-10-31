package org.carlspring.strongbox.controller;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.security.exceptions.AuthenticationException;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.metadata.MetadataType;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Martin Todorov
 */
@Controller
@RequestMapping("/metadata")
@PreAuthorize("hasAuthority('ROOT')")
public class MetadataManagementController
        extends BaseArtifactController
{

    private static final Logger logger = LoggerFactory.getLogger(MetadataManagementController.class);

    public final static String ROOT_CONTEXT = "/metadata";

    @Autowired
    private ArtifactMetadataService artifactMetadataService;

    @ApiOperation(value = "Used to rebuild the metadata for a given path.", position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The metadata was successfully rebuilt!"),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('MANAGEMENT_REBUILD_METADATA')")
    @RequestMapping(value = "{storageId}/{repositoryId}/**",
                    method = RequestMethod.POST,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity rebuild(@PathVariable String storageId,
                                  @PathVariable String repositoryId,
                                  HttpServletRequest request)
            throws IOException,
                   AuthenticationException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        try
        {
            String path = convertRequestToPath(ROOT_CONTEXT, request, storageId, repositoryId);
            artifactMetadataService.rebuildMetadata(storageId, repositoryId, path);

            return ResponseEntity.ok("The metadata was successfully rebuilt!");
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @ApiOperation(value = "Used to delete metadata entries for an artifact", position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully removed metadata entry."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('MANAGEMENT_DELETE_METADATA')")
    @RequestMapping(value = "{storageId}/{repositoryId}/**",
                    method = RequestMethod.DELETE,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity delete(@PathVariable String storageId,
                                 @PathVariable String repositoryId,
                                 @RequestParam(name = "version") String version,
                                 @RequestParam(name = "classifier") String classifier,
                                 @RequestParam(name = "metadataType") String metadataType,
                                 HttpServletRequest request)
            throws IOException,
                   AuthenticationException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        logger.debug("[delete] storageId " + storageId + " repositoryId " + repositoryId + " version " + version);

        try
        {
            String path = convertRequestToPath(ROOT_CONTEXT, request, storageId, repositoryId);
            if (ArtifactUtils.isReleaseVersion(version))
            {
                artifactMetadataService.removeVersion(storageId,
                                                      repositoryId,
                                                      path,
                                                      version,
                                                      MetadataType.from(metadataType));
            }
            else
            {
                artifactMetadataService.removeTimestampedSnapshotVersion(storageId,
                                                                         repositoryId,
                                                                         path,
                                                                         version,
                                                                         classifier);
            }

            return ResponseEntity.ok("Successfully removed metadata entry.");
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
