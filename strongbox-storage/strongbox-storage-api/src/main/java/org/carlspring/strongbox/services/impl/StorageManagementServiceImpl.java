package org.carlspring.strongbox.services.impl;

import java.io.IOException;

import javax.inject.Inject;

import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.StorageDto;
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

    protected static final Logger logger = LoggerFactory.getLogger(StorageManagementServiceImpl.class);

    @Inject
    private ConfigurationManagementService configurationManagementService;
    
    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Override
    public void saveStorage(StorageDto storage)
            throws IOException
    {
        configurationManagementService.saveStorage(storage);
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
    }

}

