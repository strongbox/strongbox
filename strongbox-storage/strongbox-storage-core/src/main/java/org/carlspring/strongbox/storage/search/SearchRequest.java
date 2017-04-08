package org.carlspring.strongbox.storage.search;

import org.carlspring.strongbox.providers.search.OrientDbSearchProvider;

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

}
