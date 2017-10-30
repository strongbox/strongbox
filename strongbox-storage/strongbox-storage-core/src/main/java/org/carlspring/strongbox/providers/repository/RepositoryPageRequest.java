package org.carlspring.strongbox.providers.repository;

public class RepositoryPageRequest
{

    public static final Integer MAX_LIMIT = 1000;

    private Integer skip;
    private Integer limit;
    private String orderBy;

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

}
