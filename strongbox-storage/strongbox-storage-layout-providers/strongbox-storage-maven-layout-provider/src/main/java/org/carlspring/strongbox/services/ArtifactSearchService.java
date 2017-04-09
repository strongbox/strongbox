package org.carlspring.strongbox.services;

import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResults;

/**
 * @author mtodorov
 */
public interface ArtifactSearchService
{

    SearchResults search(SearchRequest searchRequest)
            throws SearchException;

    boolean contains(SearchRequest searchRequest)
            throws SearchException;

}
