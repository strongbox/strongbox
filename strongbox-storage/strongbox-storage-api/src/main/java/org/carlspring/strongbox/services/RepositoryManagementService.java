package org.carlspring.strongbox.services;

import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.storage.Storage;

import java.io.IOException;

/**
 * @author mtodorov
 */
public interface RepositoryManagementService
{

    void createRepository(String storageId,
                          String repositoryId)
            throws IOException;

    void removeRepository(String storageId,
                          String repositoryId)
            throws IOException;

    void deleteTrash(String storageId, String repositoryId)
            throws IOException;

    void deleteTrash()
            throws IOException;

    void undelete(String storageId,
                  String repositoryId,
                  String artifactPath)
            throws IOException;

    void undeleteTrash(String storageId, String repositoryId)
            throws IOException,
                   ProviderImplementationException;

    void undeleteTrash()
            throws IOException,
                   ProviderImplementationException;

    Storage getStorage(String storageId);

}
