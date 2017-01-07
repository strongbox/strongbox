package org.carlspring.strongbox.services;

import org.carlspring.strongbox.storage.ArtifactStorageException;

import java.io.IOException;

import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * @author mtodorov
 */
public interface RepositoryManagementService
{

    void createRepository(String storageId, String repositoryId)
            throws IOException;

    void createRemoteRepository(String storageId,
                                String repositoryId,
                                String remoteUrl)
            throws IOException, PlexusContainerException, ComponentLookupException;

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

}
