package org.carlspring.strongbox.storage.validation.resource;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class ArtifactOperationsValidator
{


    @Autowired
    private ConfigurationManager configurationManager;


    public ArtifactOperationsValidator()
    {
    }

    public void validate(String storageId, String repositoryId, String artifactPath)
            throws ArtifactResolutionException
    {
        checkStorageExists(storageId);
        checkRepositoryExists(storageId, repositoryId);
        checkArtifactPath(artifactPath);
    }

    public void checkStorageExists(String storageId)
            throws ArtifactResolutionException
    {
        if (storageId == null)
        {
            throw new ArtifactResolutionException("No storage specified.");
        }

        if (getConfiguration().getStorage(storageId) == null)
        {
            throw new ArtifactResolutionException("Storage " + storageId + " does not exist.");
        }
    }

    public void checkRepositoryExists(String storageId, String repositoryId)
            throws ArtifactResolutionException
    {
        if (repositoryId == null)
        {
            throw new ArtifactResolutionException("No repository specified.");
        }

        if (getConfiguration().getStorage(storageId).getRepository(repositoryId) == null)
        {
            throw new ArtifactResolutionException("Repository " + repositoryId + " does not exist.");
        }
    }

    public void checkArtifactPath(String artifactPath)
            throws ArtifactResolutionException
    {
        if (artifactPath == null)
        {
            throw new ArtifactResolutionException("No artifact path specified.");
        }
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
