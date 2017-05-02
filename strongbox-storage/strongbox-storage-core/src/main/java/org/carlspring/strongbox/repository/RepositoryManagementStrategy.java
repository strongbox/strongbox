package org.carlspring.strongbox.repository;

import java.io.IOException;

/**
 * @author carlspring
 */
public interface RepositoryManagementStrategy
{

    void createRepository(String storageId,
                          String repositoryId)
            throws IOException;

    void createRepositoryStructure(String storageBasedirPath,
                                   String repositoryId)
            throws IOException;

    void initializeRepository(String storageId,
                              String repositoryId)
            throws RepositoryInitializationException;

    void removeRepository(String storageId,
                          String repositoryId)
            throws IOException;

    void removeDirectoryStructure(String storageId,
                                  String repositoryId)
            throws IOException;

}
