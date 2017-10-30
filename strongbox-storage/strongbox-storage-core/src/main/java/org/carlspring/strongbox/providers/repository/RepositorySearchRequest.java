package org.carlspring.strongbox.providers.repository;

import java.util.HashMap;
import java.util.Map;

public class RepositorySearchRequest
{
    private String storageId;
    private String repositoryId;
    private Map<String, String> coordinates = new HashMap<>();
    private int skip;
    private int limit = -1;
    private String orderBy;
    private boolean strict;

    public RepositorySearchRequest(String storageId,
                                   String repositoryId)
    {
        super();
        this.storageId = storageId;
        this.repositoryId = repositoryId;
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

    public Map<String, String> getCoordinates()
    {
        return coordinates;
    }

    public void setCoordinates(Map<String, String> coordinates)
    {
        this.coordinates = coordinates;
    }

    public int getSkip()
    {
        return skip;
    }

    public void setSkip(int skip)
    {
        this.skip = skip;
    }

    public int getLimit()
    {
        return limit;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    public String getOrderBy()
    {
        return orderBy;
    }

    public void setOrderBy(String orderBy)
    {
        this.orderBy = orderBy;
    }

    public boolean isStrict()
    {
        return strict;
    }

    public void setStrict(boolean strict)
    {
        this.strict = strict;
    }

}
