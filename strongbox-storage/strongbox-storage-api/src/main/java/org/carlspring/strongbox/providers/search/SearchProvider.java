package org.carlspring.strongbox.providers.search;

import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.storage.search.SearchResults;

/**
 * @author carlspring
 */
public interface SearchProvider
{
    String getAlias();

    SearchResults search(SearchRequest searchRequest)
            throws SearchException;

    /**
     * This method should be used only for exact queries where preliminary results
     * have found a list of matches. It should use full artifact coordinates,
     * as the purpose of this is so that more details such as the dependency snippets
     * could be provided in the result.
     *
     * @param searchRequest
     * @return
     */
    SearchResult findExact(SearchRequest searchRequest);

    boolean contains(SearchRequest searchRequest)
            throws SearchException;

}
