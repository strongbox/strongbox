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

    protected String getStorageId()
    {
        return storageId;
    }

    protected void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    protected String getRepositoryId()
    {
        return repositoryId;
    }

    protected void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    protected Map<String, String> getCoordinates()
    {
        return coordinates;
    }

    protected void setCoordinates(Map<String, String> coordinates)
    {
        this.coordinates = coordinates;
    }

    protected int getSkip()
    {
        return skip;
    }

    protected void setSkip(int skip)
    {
        this.skip = skip;
    }

    protected int getLimit()
    {
        return limit;
    }

    protected void setLimit(int limit)
    {
        this.limit = limit;
    }

    protected String getOrderBy()
    {
        return orderBy;
    }

    protected void setOrderBy(String orderBy)
    {
        this.orderBy = orderBy;
    }

    protected boolean isStrict()
    {
        return strict;
    }

    protected void setStrict(boolean strict)
    {
        this.strict = strict;
    }

}
