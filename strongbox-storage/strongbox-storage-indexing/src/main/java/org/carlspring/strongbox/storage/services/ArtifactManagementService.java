package org.carlspring.strongbox.storage.services;

import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.ArtifactStorageException;

import java.io.InputStream;

/**
 * @author mtodorov
 */
public interface ArtifactManagementService
{

    void store(String storage,
               String repositoryName,
               String path,
               InputStream is)
            throws ArtifactStorageException;

    InputStream resolve(String storage,
                        String repository,
                        String path)
            throws ArtifactResolutionException;

    void delete(String storage,
                String repositoryName,
                String artifactPath)
            throws ArtifactStorageException;

    void deleteTrash(String storage, String repositoryName)
            throws ArtifactStorageException;

    void deleteTrash()
            throws ArtifactStorageException;

}
