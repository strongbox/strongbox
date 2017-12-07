package org.carlspring.strongbox.providers.datastore;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.services.ConfigurationManagementService;

import javax.inject.Inject;

/**
 * @author carlspring
 */
public abstract class AbstractStorageProvider
        implements StorageProvider
{

    @Inject
    private StorageProviderRegistry storageProviderRegistry;

    @Inject
    private ConfigurationManagementService configurationManagementService;


    public StorageProviderRegistry getStorageProviderRegistry()
    {
        return storageProviderRegistry;
    }

    public void setStorageProviderRegistry(StorageProviderRegistry storageProviderRegistry)
    {
        this.storageProviderRegistry = storageProviderRegistry;
    }

    public Configuration getConfiguration()
    {
        return configurationManagementService.getConfiguration();
    }

}
