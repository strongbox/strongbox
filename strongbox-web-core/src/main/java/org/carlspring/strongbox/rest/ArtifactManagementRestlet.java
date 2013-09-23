package org.carlspring.strongbox.rest;

import org.apache.maven.artifact.Artifact;
import org.carlspring.strongbox.annotations.ArtifactExistenceState;
import org.carlspring.strongbox.annotations.ArtifactResource;
import org.carlspring.strongbox.annotations.ArtifactResourceMapper;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author Martin Todorov
 */
@Path("/manage/artifact")
public class ArtifactManagementRestlet
        extends BaseRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactManagementRestlet.class);


    @POST
    @Path("/{repository}/state/{state}/length/{length}/{path:.*}")
    @Consumes(MediaType.TEXT_PLAIN)
    public String addArtifact(@PathParam("repository") String repository,
                              @PathParam("state") String state,
                              @PathParam("length") long length,
                              @PathParam("path") String artifactPath)
            throws IOException
    {
        if (!ArtifactUtils.isArtifact(artifactPath))
        {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        Artifact artifact = ArtifactUtils.convertPathToArtifact(artifactPath);

        ArtifactExistenceState existenceState = ArtifactExistenceState.valueOf(state);

        final ArtifactResource artifactResource = ArtifactResourceMapper.getArtifactResourceInstance(repository,
                                                                                                     artifact,
                                                                                                     length,
                                                                                                     existenceState);

        ArtifactResourceMapper.addResource(artifactResource);

        final String msg = "Added artifact " + artifact.toString() +
                           " with state " + state +
                           " and length " + length +
                           " to repository " + repository + ".";

        logger.debug(msg);

        return msg;
    }

    @DELETE
    @Path("{repository}/{path:.*}")
    public void delete(@PathParam("repository") String repository,
                       @PathParam("path") String path)
            throws IOException
    {
        if (!ArtifactUtils.isArtifact(path))
        {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        Artifact artifact = ArtifactUtils.convertPathToArtifact(path);
        ArtifactResourceMapper.removeResources(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());

        logger.debug("Removed artifact " + artifact.toString() + ".");
    }

}
