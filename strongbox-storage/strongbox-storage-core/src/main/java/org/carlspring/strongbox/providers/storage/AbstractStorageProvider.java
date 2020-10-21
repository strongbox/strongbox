package org.carlspring.strongbox.providers.storage;

import javax.inject.Inject;

/**
 * @author carlspring
 */
public abstract class AbstractStorageProvider
        implements StorageProvider
{

    @Inject
    private StorageProviderRegistry storageProviderRegistry;


    public StorageProviderRegistry getStorageProviderRegistry()
    {
        return storageProviderRegistry;
    }

    public void setStorageProviderRegistry(StorageProviderRegistry storageProviderRegistry)
    {
        this.storageProviderRegistry = storageProviderRegistry;
    }

}
