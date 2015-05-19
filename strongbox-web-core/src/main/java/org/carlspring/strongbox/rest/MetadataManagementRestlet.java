package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.security.jaas.authentication.AuthenticationException;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
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
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

/**
 * @author Martin Todorov
 */
@Component
@Path("/metadata")
public class MetadataManagementRestlet
        extends BaseRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(MetadataManagementRestlet.class);

    @Autowired
    private ArtifactMetadataService artifactMetadataService;


    @POST
    @Path("{storageId}/{repositoryId}/{path:.*}")
    public Response rebuild(@PathParam("storageId") String storageId,
                            @PathParam("repositoryId") String repositoryId,
                            @PathParam("path") String path,
                            @Context HttpHeaders headers,
                            @Context HttpServletRequest request,
                            InputStream is)
            throws IOException,
                   AuthenticationException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        handleAuthentication(storageId, repositoryId, path, headers, request);

        try
        {
            artifactMetadataService.rebuildMetadata(storageId, repositoryId, path);

            return Response.ok().build();
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("{storageId}/{repositoryId}/{path:.*}")
    public Response delete(@PathParam("storageId") String storageId,
                           @PathParam("path") String path,
                           @QueryParam("version") String version,
                           @PathParam("repositoryId") String repositoryId,
                           @Context HttpHeaders headers,
                           @Context HttpServletRequest request,
                           InputStream is)
            throws IOException,
                   AuthenticationException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        handleAuthentication(storageId, repositoryId, path, headers, request);

        try
        {
            artifactMetadataService.removeVersion(storageId, repositoryId, path, version);

            return Response.ok().build();
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
