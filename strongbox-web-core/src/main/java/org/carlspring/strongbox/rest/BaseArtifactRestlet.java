package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component

public class BaseArtifactRestlet extends BaseRestlet
{

    @Autowired
    private ArtifactManagementService artifactManagementService;

    @Autowired
    private MetadataManager metadataManager;


    public Storage getStorage(String storageId)
    {
        return getConfiguration().getStorage(storageId);
    }

    public Repository getRepository(String storageId, String repositoryId)
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
