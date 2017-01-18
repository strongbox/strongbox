package org.carlspring.strongbox.services;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.storage.ArtifactStorageException;

import java.io.IOException;

import org.apache.maven.index.context.IndexingContext;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * @author mtodorov
 */
public interface RepositoryManagementService
{

    void createRepository(String storageId, String repositoryId)
            throws IOException, ArtifactTransportException;

    int reIndex(String storageId,
                String repositoryId,
                String path)
            throws IOException;

    void mergeIndexes(String sourceStorage,
                      String sourceRepositoryId,
                      String targetStorage,
                      String targetRepositoryId)
            throws ArtifactStorageException;

    void pack(String storageId, String repositoryId)
            throws IOException;

    void removeRepository(String storageId,
                          String repositoryId)
            throws IOException;

    /**
     * Download remote repository index
     *
     * @param storageId    String
     * @param repositoryId String
     */
    void downloadRemoteIndex(String storageId,
                             String repositoryId)
            throws ArtifactTransportException;

    IndexingContext getRemoteRepositoryIndexingContext(String storageId,
                                                       String repositoryId)
            throws ArtifactTransportException;

}
