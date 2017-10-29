package org.carlspring.strongbox.providers.repository;

import java.util.HashMap;
import java.util.Map;

public class RepositorySearchRequest
{
    public static final Integer MAX_LIMIT = 1000;

    private String storageId;
    private String repositoryId;
    private Map<String, String> coordinates = new HashMap<>();
    private Integer skip;
    private Integer limit;
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

    public Integer getSkip()
    {
        return skip == null ? 0 : skip;
    }

    public void setSkip(Integer skip)
    {
        this.skip = skip;
    }

    public Integer getLimit()
    {
        return limit == null || limit < 0 || limit > MAX_LIMIT ? MAX_LIMIT : limit;
    }

    public void setLimit(Integer limit)
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
