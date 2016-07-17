package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.providers.AbstractMappedProviderRegistry;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * @author carlspring
 */
@Component("repositoryProviderRegistry")
public class RepositoryProviderRegistry extends AbstractMappedProviderRegistry<RepositoryProvider>
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryProviderRegistry.class);

    @Autowired
    private StorageProviderRegistry storageProviderRegistry;


    public RepositoryProviderRegistry()
    {
    }

    @Override
    @PostConstruct
    public void initialize()
    {
        logger.info("Initialized the repository provider registry.");
    }

    /**
     * K: String   :
     * V: Provider : The provider
     *
     * @return
     */
    @Override
    public Map<String, RepositoryProvider> getProviders()
    {
        return super.getProviders();
    }

    @Override
    public void setProviders(Map<String, RepositoryProvider> providers)
    {
        super.setProviders(providers);
    }

    @Override
    public RepositoryProvider getProvider(String alias)
    {
        return super.getProvider(alias);
    }

    @Override
    public RepositoryProvider addProvider(String alias, RepositoryProvider provider)
    {
        return super.addProvider(alias, provider);
    }

    @Override
    public void removeProvider(String alias)
    {
        super.removeProvider(alias);
    }

}
