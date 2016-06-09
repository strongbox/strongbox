package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("storageManagementService")
public class StorageManagementServiceImpl implements StorageManagementService
{

    private static final Logger logger = LoggerFactory.getLogger(StorageManagementServiceImpl.class);

    @Autowired
    private ConfigurationManager configurationManager;

    @Autowired
    private RepositoryManagementService repositoryManagementService;


    @Override
    public void createStorage(Storage storage)
            throws IOException
    {
        final File storageBaseDir = new File(storage.getBasedir());

        logger.debug("Creating directory for storage '" + storage.getId() +
                     "' (" + storageBaseDir.getAbsolutePath() + ")...");

        //noinspection ResultOfMethodCallIgnored
        storageBaseDir.mkdirs();
    }

    @Override
    public void removeStorage(String storageId)
            throws IOException
    {
        final Storage storage = configurationManager.getConfiguration().getStorage(storageId);
        for (Repository repository : storage.getRepositories().values())
        {
            repositoryManagementService.removeRepository(storageId, repository.getId());
        }

        FileUtils.deleteDirectory(new File(storage.getBasedir()));
    }

}

