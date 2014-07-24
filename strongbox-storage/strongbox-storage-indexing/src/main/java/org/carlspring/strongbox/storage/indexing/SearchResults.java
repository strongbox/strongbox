package org.carlspring.strongbox.storage.indexing;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.index.ArtifactInfo;

/**
 * @author mtodorov
 */
public class SearchResults
{

    /**
     * K: storage:repository
     * V: artifactInfos
     */
    private Map<String, Collection<ArtifactInfo>> results = new LinkedHashMap<>();


    public SearchResults()
    {
    }

    public Map<String, Collection<ArtifactInfo>> getResults()
    {
        return results;
    }

    public void setResults(Map<String, Collection<ArtifactInfo>> results)
    {
        this.results = results;
    }

}
