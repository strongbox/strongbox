package org.carlspring.strongbox.services.impl;

import java.io.IOException;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.storage.Storage;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("nugetArtifactManagementService")
public class NugetArtifactManagementService
        extends AbstractArtifactManagementService
{

    @Override
    public void delete(String storageId,
                       String repositoryId,
                       String artifactPath,
                       boolean force)
            throws IOException
    {
        throw new UnsupportedOperationException("This operation is not yet implemented!");
    }

    @Override
    public boolean contains(String storageId,
                            String repositoryId,
                            String artifactPath)
            throws IOException
    {
        throw new UnsupportedOperationException("This operation is not yet implemented!");
    }

    @Override
    public void copy(String srcStorageId,
                     String srcRepositoryId,
                     String path,
                     String destStorageId,
                     String destRepositoryId)
            throws IOException
    {
        throw new UnsupportedOperationException("This operation is not yet implemented!");
    }

    @Override
    public void removeTimestampedSnapshots(String storageId,
                                           String repositoryId,
                                           String artifactPath,
                                           int numberToKeep,
                                           int keepPeriod)
            throws IOException
    {
        throw new UnsupportedOperationException("This operation is not yet implemented!");
    }

    @Override
    public Storage getStorage(String storageId)
    {
        return getConfiguration().getStorages().get(storageId);
    }

    @Override
    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
