package org.carlspring.strongbox.services;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.storage.Storage;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

/**
 * @author mtodorov
 */
public interface ArtifactManagementService extends ConfigurationService
{

    void store(String storageId,
               String repositoryId,
               String path,
               InputStream is)
            throws IOException, ProviderImplementationException, NoSuchAlgorithmException;

    InputStream resolve(String storageId,
                        String repositoryId,
                        String path)
            throws IOException, ArtifactTransportException, ProviderImplementationException;

    void delete(String storageId,
                String repositoryId,
                String artifactPath,
                boolean force)
            throws IOException;

    boolean contains(String storageId,
                     String repositoryId,
                     String artifactPath)
            throws IOException;

    void copy(String srcStorageId,
              String srcRepositoryId,
              String path,
              String destStorageId,
              String destRepositoryId)
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
            throws IOException, ProviderImplementationException;

    void undeleteTrash()
            throws IOException, ProviderImplementationException;

    Storage getStorage(String storageId);

}
