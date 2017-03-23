package org.carlspring.strongbox.storage.indexing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.util.StringUtils;

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

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        for (SearchResult artifact : results)
        {
            sb.append(artifact.getStorageId()).append(':').append(artifact.getRepositoryId()).append(' ');
            sb.append(artifact.getGroupId()).append(':');
            sb.append(artifact.getArtifactId()).append(':');
            sb.append(artifact.getVersion()).append(':');
            sb.append(artifact.getExtension());
            if (!StringUtils.isEmpty(artifact.getClassifier()))
            {
                sb.append(':');
                sb.append(artifact.getClassifier());
            }
            sb.append(' ');
            sb.append(artifact.getUrl());
            sb.append('\n');
        }

        return sb.toString();
    }

}
