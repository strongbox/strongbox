package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.providers.search.OrientDbSearchProvider;
import org.carlspring.strongbox.providers.search.SearchException;
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
    private OrientDbSearchProvider orientDbSearchProvider;


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