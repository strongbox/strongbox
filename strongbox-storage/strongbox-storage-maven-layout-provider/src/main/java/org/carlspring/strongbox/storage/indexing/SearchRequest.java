package org.carlspring.strongbox.storage.indexing;

/**
 * @author mtodorov
 */
public class SearchRequest
{

    private String storageId;

    private String repositoryId;

    private String query;


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

}
