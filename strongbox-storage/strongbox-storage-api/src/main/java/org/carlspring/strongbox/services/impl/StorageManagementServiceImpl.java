package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("storageManagementService")
public class StorageManagementServiceImpl implements StorageManagementService
{

    private static final Logger logger = LoggerFactory.getLogger(StorageManagementServiceImpl.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private RepositoryManagementService repositoryManagementService;


    @Override
    public void createStorage(MutableStorage storage)
            throws IOException
    {
        Path storageBaseDir = Paths.get(storage.getBasedir());
        if (!Files.exists(storageBaseDir))
        {
            logger.debug("Creating directory for storage '" + storage.getId() + "' (" + storageBaseDir + ")...");
            Files.createDirectories(storageBaseDir);
        }
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

        Path storageBaseDir = Paths.get(storage.getBasedir());
        if (!Files.exists(storageBaseDir))
        {
            logger.debug("Deleting directory for storage '" + storage.getId() + "' (" + storageBaseDir + ")...");
            Files.delete(storageBaseDir);
        }
    }

}

