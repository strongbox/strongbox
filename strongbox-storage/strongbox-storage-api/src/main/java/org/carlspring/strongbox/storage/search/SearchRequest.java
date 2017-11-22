package org.carlspring.strongbox.storage.search;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.providers.search.OrientDbSearchProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mtodorov
 */
public class SearchRequest
{

    private String storageId;

    private String repositoryId;

    private String query;

    /**
     * The search provider implementation to use. This defaults to the database one.
     */
    private String implementation = OrientDbSearchProvider.ALIAS;

    private Map<String, String> options = new LinkedHashMap<>();

    private ArtifactCoordinates artifactCoordinates;


    public SearchRequest()
    {
    }

    public SearchRequest(String storageId,
                         String repositoryId,
                         String query)
    {
        this.storageId = storageId;
        this.repositoryId = repositoryId;
        this.query = query;
    }

    public SearchRequest(String storageId,
                         String repositoryId,
                         String query,
                         String implementation)
    {
        this.storageId = storageId;
        this.repositoryId = repositoryId;
        this.query = query;
        this.implementation = implementation;
    }

    public SearchRequest(String storageId,
                         String repositoryId,
                         ArtifactCoordinates coordinates,
                         String implementation)
    {
        this.storageId = storageId;
        this.repositoryId = repositoryId;
        this.artifactCoordinates = coordinates;
        this.implementation = implementation;
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

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public String getImplementation()
    {
        return implementation;
    }

    public void setImplementation(String implementation)
    {
        this.implementation = implementation;
    }

    public Map<String, String> getOptions()
    {
        return options;
    }

    public void setOptions(Map<String, String> options)
    {
        this.options = options;
    }

    public String getOption(String key)
    {
        return options.get(key);
    }

    public String addOption(String key,
                            String value)
    {
        return options.put(key, value);
    }

    public boolean removeOption(String key,
                                String value)
    {
        return options.remove(key, value);
    }

    public ArtifactCoordinates getArtifactCoordinates()
    {
        return artifactCoordinates;
    }

    public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        this.artifactCoordinates = artifactCoordinates;
    }
    
}
