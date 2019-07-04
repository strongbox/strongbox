package org.carlspring.strongbox.services;

import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.storage.Storage;

import java.io.IOException;

/**
 * @author mtodorov
 */
public interface RepositoryManagementService
{

    void createRepository(String storageId,
                          String repositoryId)
            throws IOException, RepositoryManagementStrategyException;

    void removeRepository(String storageId,
                          String repositoryId)
            throws IOException;

    void deleteTrash(String storageId, String repositoryId)
            throws IOException;

    void deleteTrash()
            throws IOException;

    void undelete(RepositoryPath repositoryPath)
            throws IOException;

    void undeleteTrash(String storageId, String repositoryId)
            throws IOException,
                   ProviderImplementationException;

    void undeleteTrash()
            throws IOException,
                   ProviderImplementationException;

    void putInService(String storageId, String repositoryId) throws IOException;

    void putOutOfService(String storageId, String repositoryId) throws IOException;

    Storage getStorage(String storageId);

}
