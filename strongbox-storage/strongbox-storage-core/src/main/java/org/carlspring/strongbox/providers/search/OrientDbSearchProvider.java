package org.carlspring.strongbox.providers.search;

import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResults;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("orientDbSearchProvider")
public class OrientDbSearchProvider implements SearchProvider
{

    private static final Logger logger = LoggerFactory.getLogger(OrientDbSearchProvider.class);

    public static final String ALIAS = "OrientDB";

    @Inject
    private SearchProviderRegistry searchProviderRegistry;


    @PostConstruct
    @Override
    public void register()
    {
        searchProviderRegistry.addProvider(ALIAS, this);

        logger.info("Registered search provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public SearchResults search(SearchRequest searchRequest)
            throws SearchException
    {
        // TODO: Implement
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public boolean contains(SearchRequest searchRequest)
            throws SearchException
    {
        // TODO: Implement
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
