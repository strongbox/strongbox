package org.carlspring.strongbox.services;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.RepositoryInitializationException;

import java.io.IOException;

/**
 * @author mtodorov
 */
public interface RepositoryManagementService
{

    void createRepository(String storageId, String repositoryId)
            throws IOException;

    int reIndex(String storageId,
                String repositoryId,
                String path)
            throws IOException;

    void mergeIndexes(String sourceStorage,
                      String sourceRepositoryId,
                      String targetStorage,
                      String targetRepositoryId)
            throws ArtifactStorageException;

    void pack(String storageId,
              String repositoryId)
            throws IOException;

    void removeRepository(String storageId,
                          String repositoryId)
            throws IOException;

    void downloadRemoteIndex(String storageId,
                             String repositoryId)
            throws ArtifactTransportException, RepositoryInitializationException;

}
