package org.carlspring.strongbox.providers.search;

import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResults;

/**
 * @author carlspring
 */
public interface SearchProvider
{

    void register();

    String getAlias();

    SearchResults search(SearchRequest searchRequest)
            throws SearchException;

    boolean contains(SearchRequest searchRequest)
            throws SearchException;

}
