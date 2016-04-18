package org.carlspring.strongbox.rest;

import io.swagger.annotations.*;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.security.jaas.authentication.AuthenticationException;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.resolvers.ArtifactStorageException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

/**
 * @author Martin Todorov
 */
@Component
@Path("/metadata")
@Api(value = "/metadata")
public class MetadataManagementRestlet
        extends BaseRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(MetadataManagementRestlet.class);

    @Autowired
    private ArtifactMetadataService artifactMetadataService;


    @POST
    @Path("{storageId}/{repositoryId}/{path:.*}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Used to rebuild the metadata for a given path.", position = 0, produces = MediaType.TEXT_PLAIN)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The metadata was successfully rebuilt!"),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    public Response rebuild(@ApiParam(value = "The storageId", required = true)
                            @PathParam("storageId") String storageId,
                            @ApiParam(value = "The repositoryId", required = true)
                            @PathParam("repositoryId") String repositoryId,
                            @ApiParam(value = "The path to the artifact.", required = true)
                            @PathParam("path") String path,
                            @Context HttpHeaders headers,
                            @Context HttpServletRequest request,
                            InputStream is)
            throws IOException,
                   AuthenticationException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        try
        {
            artifactMetadataService.rebuildMetadata(storageId, repositoryId, path);

            return Response.ok().entity("The metadata was successfully rebuilt!").build();
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("{storageId}/{repositoryId}/{path:.*}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Used to delete metadata entries for an artifact", position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully removed metadata entry."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    public Response delete(@ApiParam(value = "The storageId", required = true)
                           @PathParam("storageId") String storageId,
                           @ApiParam(value = "The repositoryId", required = true)
                           @PathParam("repositoryId") String repositoryId,
                           @ApiParam(value = "The path to the artifact.", required = true)
                           @PathParam("path") String path,
                           @ApiParam(value = "The version of the artifact.", required = true)
                           @QueryParam("version") String version,
                           @ApiParam(value = "The classifier of the artifact.")
                           @QueryParam("classifier") String classifier,
                           @ApiParam(value = "The type of metadata (artifact/snapshot/plugin).")
                           @QueryParam("metadataType") String metadataType,
                           @Context HttpHeaders headers,
                           @Context HttpServletRequest request,
                           InputStream is)
            throws IOException,
                   AuthenticationException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        try
        {
            if (ArtifactUtils.isReleaseVersion(version))
            {
                artifactMetadataService.removeVersion(storageId, repositoryId, path, version, MetadataType.from(metadataType));
            }
            else
            {
                artifactMetadataService.removeTimestampedSnapshotVersion(storageId, repositoryId, path, version, classifier);
            }

            return Response.ok().entity("Successfully removed metadata entry.").build();
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

}
