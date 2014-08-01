package org.carlspring.strongbox.storage.services;

import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;

/**
 * @author mtodorov
 */
public interface RepositoryManagementService
{

    void createRepository(String storageId, String repositoryId)
            throws IOException;

    void updateRepository(String storageId, Repository repositoryId);

    void mergeRepositoryIndex(String sourceStorageId,
                              Repository sourceRepositoryId,
                              String destinationStorageId,
                              Repository destinationRepositoryId);

    void deleteRepository(String storageId, String repositoryId);

}
