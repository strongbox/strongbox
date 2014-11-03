package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexerFactory;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryManager;
import org.carlspring.strongbox.services.RepositoryManagementService;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class RepositoryManagementServiceImpl
        implements RepositoryManagementService
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryManagementServiceImpl.class);

    @Autowired
    private DataCenter dataCenter;

    @Autowired
    private RepositoryManager repositoryManager;

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;

    @Autowired
    private RepositoryIndexerFactory repositoryIndexerFactory;


    @Override
    public void createRepository(String storageId,
                                 String repositoryId)
            throws IOException
    {
        Storage storage = dataCenter.getStorage(storageId);

        final String storageBasedirPath = storage.getBasedir();
        final File storageBasedir = new File(storageBasedirPath).getAbsoluteFile();
        final File repositoryBasedir = new File(storageBasedirPath, repositoryId).getAbsoluteFile();

        logger.debug("Creating repository '" + storageBasedir.getAbsolutePath() + File.separatorChar + repositoryId + "'...");

        repositoryManager.createRepositoryStructure(storageBasedir.getAbsolutePath(), repositoryId);

        final File indexDir = new File(repositoryBasedir, ".index");

        RepositoryIndexer repositoryIndexer = repositoryIndexerFactory.createRepositoryIndexer(storageId,
                                                                                               repositoryId,
                                                                                               repositoryBasedir,
                                                                                               indexDir);

        repositoryIndexManager.addRepositoryIndex("storage0:" + repositoryId, repositoryIndexer);

        logger.debug("Created repository '" + storageBasedir.getAbsolutePath() + File.separatorChar + repositoryId + "'.");
    }

    @Override
    public void updateRepository(String storageId,
                                 Repository repositoryId)
    {
        // TODO: Implement
    }

    @Override
    public void mergeRepositoryIndex(String sourceStorageId,
                                     Repository sourceRepositoryId,
                                     String destinationStorageId,
                                     Repository destinationRepositoryId)
    {
        // TODO: Implement

    }

    @Override
    public void deleteRepository(String storageId,
                                 String repositoryId)
    {
        // TODO: Implement
    }

}
