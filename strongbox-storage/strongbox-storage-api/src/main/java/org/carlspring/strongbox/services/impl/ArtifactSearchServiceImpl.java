package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.providers.search.SearchProvider;
import org.carlspring.strongbox.providers.search.SearchProviderRegistry;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResults;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class ArtifactSearchServiceImpl
        implements ArtifactSearchService
{

    @Inject
    private SearchProviderRegistry searchProviderRegistry;


    @Override
    public SearchResults search(SearchRequest searchRequest)
            throws SearchException
    {
        SearchProvider searchProvider = searchProviderRegistry.getProvider(searchRequest.getImplementation());

        return searchProvider.search(searchRequest);
    }

    @Override
    public boolean contains(SearchRequest searchRequest)
            throws SearchException
    {
        return !search(searchRequest).getResults()
                                     .isEmpty();
    }

}