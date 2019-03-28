package org.carlspring.strongbox.services.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
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
    private ConfigurationManagementService configurationManagementService;
    
    @Inject
    private RepositoryManagementService repositoryManagementService;


    @Override
    public void saveStorage(MutableStorage storage)
            throws IOException
    {
        configurationManagementService.saveStorage(storage);
        
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
        final Storage storage = configurationManagementService.getConfiguration().getStorage(storageId);
        for (Repository repository : storage.getRepositories().values())
        {
            repositoryManagementService.removeRepository(storageId, repository.getId());
        }

        Path storageBaseDir = Paths.get(storage.getBasedir());
        if (Files.exists(storageBaseDir))
        {
            logger.debug("Deleting directory for storage '" + storage.getId() + "' (" + storageBaseDir + ")...");
            Files.delete(storageBaseDir);
        }
    }

}

