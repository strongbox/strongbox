package org.carlspring.strongbox.services;

import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.ArtifactStorageException;

import java.io.InputStream;

/**
 * @author mtodorov
 */
public interface ArtifactManagementService
{

    void store(String storageId,
               String repositoryId,
               String path,
               InputStream is)
            throws ArtifactStorageException;

    InputStream resolve(String storageId,
                        String repositoryId,
                        String path)
            throws ArtifactResolutionException;

    void delete(String storageId,
                String repositoryId,
                String artifactPath,
                boolean force)
            throws ArtifactStorageException;

    void deleteTrash(String storageId, String repositoryId)
            throws ArtifactStorageException;

    void deleteTrash()
            throws ArtifactStorageException;

}
