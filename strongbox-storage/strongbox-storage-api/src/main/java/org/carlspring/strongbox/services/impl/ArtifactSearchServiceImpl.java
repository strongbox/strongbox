package org.carlspring.strongbox.services.impl;

import javax.inject.Inject;

import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.providers.search.SearchProvider;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class ArtifactSearchServiceImpl
        implements ArtifactSearchService
{

    @Inject
    @Qualifier("orientDbSearchProvider")
    private SearchProvider orientDbSearchProvider;


    @Override
    public SearchResults search(SearchRequest searchRequest)
            throws SearchException
    {
        return orientDbSearchProvider.search(searchRequest);
    }

    @Override
    public boolean contains(SearchRequest searchRequest)
            throws SearchException
    {
        return !search(searchRequest).getResults().isEmpty();
    }

}