package org.carlspring.strongbox.providers.search;

import org.carlspring.strongbox.providers.AbstractMappedProviderRegistry;

import javax.annotation.PostConstruct;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("searchProviderRegistry")
public class SearchProviderRegistry
        extends AbstractMappedProviderRegistry<SearchProvider>
{

    private static final Logger logger = LoggerFactory.getLogger(SearchProviderRegistry.class);


    public SearchProviderRegistry()
    {
    }

    @Override
    @PostConstruct
    public void initialize()
    {
        logger.info("Initialized the search provider registry.");
    }

    public void dump()
    {
        logger.debug("Listing search providers:");
        for (String providerName : getProviders().keySet())
        {
            logger.debug(" -> " + providerName);
        }
    }

    /**
     * K: String   :
     * V: Provider : The provider
     *
     * @return
     */
    @Override
    public Map<String, SearchProvider> getProviders()
    {
        return super.getProviders();
    }

    @Override
    public void setProviders(Map<String, SearchProvider> providers)
    {
        super.setProviders(providers);
    }

    @Override
    public SearchProvider getProvider(String alias)
    {
        SearchProvider provider = super.getProvider(alias);
        if (provider != null)
        {
            return provider;
        }
        else
        {
            // Fallback to using the Orient database one
            return getProvider(OrientDbSearchProvider.ALIAS);
        }
    }

    @Override
    public SearchProvider addProvider(String alias,
                                      SearchProvider provider)
    {
        return super.addProvider(alias, provider);
    }

    @Override
    public void removeProvider(String alias)
    {
        super.removeProvider(alias);
    }

}
