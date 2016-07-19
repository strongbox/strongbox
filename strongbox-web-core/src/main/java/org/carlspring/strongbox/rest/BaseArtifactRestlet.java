package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.ws.rs.core.Response;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component

public class BaseArtifactRestlet
        extends BaseRestlet
{

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private ArtifactManagementService artifactManagementService;

    @Autowired
    private MetadataManager metadataManager;

    @Autowired
    private ObjectMapper objectMapper;

    // ----------------------------------------------------------------------------------------------------------------
    // Common-purpose methods

    protected synchronized <T> T read(String json,
                                      Class<T> type)
    {
        try
        {
            return objectMapper.readValue(json, type);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected synchronized Response toResponse(Object arg)
    {
        try
        {
            return Response.ok(objectMapper.writeValueAsString(arg)).build();
        }
        catch (Exception e)
        {
            return toError(e);
        }
    }

    protected synchronized Response toError(String message)
    {
        return toError(new RuntimeException(message));
    }

    protected synchronized Response toError(Throwable cause)
    {
        logger.error(cause.getMessage(), cause);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(cause.getMessage()).build();
    }

    public Storage getStorage(String storageId)
    {
        return getConfiguration().getStorage(storageId);
    }

    public Repository getRepository(String storageId,
                                    String repositoryId)
    {
        return getStorage(storageId).getRepository(repositoryId);
    }

    public ArtifactManagementService getArtifactManagementService()
    {
        return artifactManagementService;
    }

    public void setArtifactManagementService(ArtifactManagementService artifactManagementService)
    {
        this.artifactManagementService = artifactManagementService;
    }

    public MetadataManager getMetadataManager()
    {
        return metadataManager;
    }

    public void setMetadataManager(MetadataManager metadataManager)
    {
        this.metadataManager = metadataManager;
    }

}
