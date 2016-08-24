package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.storage.Storage;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author mtodorov
 */
public abstract class AbstractLayoutProvider
        implements LayoutProvider
{

    @Autowired
    private LayoutProviderRegistry layoutProviderRegistry;

    @Autowired
    private StorageProviderRegistry storageProviderRegistry;

    @Autowired
    private ConfigurationManager configurationManager;


    public LayoutProviderRegistry getLayoutProviderRegistry()
    {
        return layoutProviderRegistry;
    }

    public void setLayoutProviderRegistry(LayoutProviderRegistry layoutProviderRegistry)
    {
        this.layoutProviderRegistry = layoutProviderRegistry;
    }

    public StorageProviderRegistry getStorageProviderRegistry()
    {
        return storageProviderRegistry;
    }

    public void setStorageProviderRegistry(StorageProviderRegistry storageProviderRegistry)
    {
        this.storageProviderRegistry = storageProviderRegistry;
    }

    public ConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    public Storage getStorage(String storageId)
    {
        return configurationManager.getConfiguration().getStorage(storageId);
    }

}
