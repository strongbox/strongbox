package org.carlspring.strongbox.storage.indexing;

import javax.xml.bind.annotation.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "artifacts")
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchResults
{

    @XmlElement(name = "artifact")
    private Set<SearchResult> results = new LinkedHashSet<>();


    public SearchResults()
    {
    }

    public Set<SearchResult> getResults()
    {
        return results;
    }

    public void setResults(Set<SearchResult> results)
    {
        this.results = results;
    }

}
