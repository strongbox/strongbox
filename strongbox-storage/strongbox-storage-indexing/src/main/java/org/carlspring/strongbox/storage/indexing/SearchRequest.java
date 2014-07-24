package org.carlspring.strongbox.storage.indexing;

/**
 * @author mtodorov
 */
public class SearchRequest
{

    private String storage;

    private String repository;

    private String query;


    public SearchRequest()
    {
    }

    public SearchRequest(String storage,
                         String repository,
                         String query)
    {
        this.storage = storage;
        this.repository = repository;
        this.query = query;
    }

    public String getStorage()
    {
        return storage;
    }

    public void setStorage(String storage)
    {
        this.storage = storage;
    }

    public String getRepository()
    {
        return repository;
    }

    public void setRepository(String repository)
    {
        this.repository = repository;
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
