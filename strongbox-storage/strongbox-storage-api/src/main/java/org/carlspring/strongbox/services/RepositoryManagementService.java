package org.carlspring.strongbox.services;

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

}
