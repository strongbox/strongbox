package org.carlspring.strongbox.storage.search;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;

import javax.xml.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "artifact")
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchResult
{

    @XmlAnyElement
    private ArtifactCoordinates artifactCoordinates;

    @XmlElement
    private String storageId;

    @XmlElement
    private String repositoryId;

    @XmlElement
    private String url;

    /**
     * K: The compatible dependency format's alias
     * V: The string representation of the snippet.
     */
    @XmlElement
    private Map<String, String> snippets = new LinkedHashMap<>();


    public SearchResult()
    {
    }

    public SearchResult(String storageId,
                        String repositoryId,
                        ArtifactCoordinates artifactCoordinates,
                        String url)
    {
        this.storageId = storageId;
        this.repositoryId = repositoryId;
        this.artifactCoordinates = artifactCoordinates;
        this.url = url;
    }

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public ArtifactCoordinates getArtifactCoordinates()
    {
        return artifactCoordinates;
    }

    public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        this.artifactCoordinates = artifactCoordinates;
    }

    public String getPath()
    {
        return getArtifactCoordinates().toPath();
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public Map<String, String> getSnippets()
    {
        return snippets;
    }

    public void setSnippets(Map<String, String> snippets)
    {
        this.snippets = snippets;
    }

    @Override
    public String toString()
    {
        return getPath();
    }

}
