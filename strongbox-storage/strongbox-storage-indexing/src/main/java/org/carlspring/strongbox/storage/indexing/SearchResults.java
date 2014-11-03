package org.carlspring.strongbox.storage.indexing;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mtodorov
 */
public class SearchResults
{

    /**
     * K: storage:repository
     * V: searchResults
     */
    private Map<String, Collection<SearchResult>> results = new LinkedHashMap<>();


    public SearchResults()
    {
    }

    public Map<String, Collection<SearchResult>> getResults()
    {
        return results;
    }

    public void setResults(Map<String, Collection<SearchResult>> results)
    {
        this.results = results;
    }

}
