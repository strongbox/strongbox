package org.carlspring.strongbox.services;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.Storage;

/**
 * @author mtodorov
 */
public interface ArtifactManagementService extends ConfigurationService
{
    /**
     * @return total number of bytes stored
     */
    long validateAndStore(String storageId,
                          String repositoryId,
                          String path,
                          InputStream is)
            throws IOException,
                   ProviderImplementationException,
                   NoSuchAlgorithmException;

    /**
     * @return total number of bytes stored
     */
    long store(RepositoryPath path,
               InputStream is)
            throws IOException,
                   ProviderImplementationException,
                   NoSuchAlgorithmException;

    InputStream resolve(String storageId,
                        String repositoryId,
                        String path)
            throws IOException,
                   ArtifactTransportException,
                   ProviderImplementationException;

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
              String destStorageId,
              String destRepositoryId,
              String path)
            throws IOException;

    void removeTimestampedSnapshots(String storageId,
                                    String repositoryId,
                                    String artifactPath,
                                    int numberToKeep,
                                    int keepPeriod)
            throws IOException;

    Storage getStorage(String storageId);

    RepositoryFileAttributes getAttributes(String storageId,
                                           String repositoryId,
                                           String path) throws ArtifactTransportException, ProviderImplementationException, NoSuchAlgorithmException;
    
}
