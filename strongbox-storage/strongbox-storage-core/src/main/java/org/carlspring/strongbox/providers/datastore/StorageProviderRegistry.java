package org.carlspring.strongbox.providers.datastore;

import org.carlspring.strongbox.providers.AbstractMappedProviderRegistry;

import javax.annotation.PostConstruct;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("storageProviderRegistry")
public class StorageProviderRegistry extends AbstractMappedProviderRegistry<StorageProvider>
{

    private static final Logger logger = LoggerFactory.getLogger(StorageProviderRegistry.class);


    public StorageProviderRegistry()
    {
    }

    @Override
    @PostConstruct
    public void initialize()
    {
        logger.info("Initialized the storage provider registry.");
    }

    @Override
    public Map<String, StorageProvider> getProviders()
    {
        return super.getProviders();
    }

    @Override
    public void setProviders(Map<String, StorageProvider> providers)
    {
        super.setProviders(providers);
    }

    @Override
    public StorageProvider getProvider(String implementation)
    {
        return super.getProvider(implementation);
    }

    @Override
    public StorageProvider addProvider(String implementation,
                                       StorageProvider provider)
    {
        return super.addProvider(implementation, provider);
    }

    @Override
    public void removeProvider(String alias)
    {
        super.removeProvider(alias);
    }

}
