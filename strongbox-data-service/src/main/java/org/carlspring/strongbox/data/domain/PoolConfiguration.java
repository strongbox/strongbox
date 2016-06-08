package org.carlspring.strongbox.data.domain;

import java.util.Objects;

/**
 * @author korest
 */
public class PoolConfiguration extends GenericEntity
{

    private String repositoryUrl;

    private int maxConnections;

    public int getMaxConnections()
    {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections)
    {
        this.maxConnections = maxConnections;
    }

    public String getRepositoryUrl()
    {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl)
    {
        this.repositoryUrl = repositoryUrl;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PoolConfiguration that = (PoolConfiguration) o;
        return maxConnections == that.maxConnections && Objects.equals(repositoryUrl, that.repositoryUrl)
                && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, repositoryUrl, maxConnections);
    }

    @Override
    public String toString()
    {
        return "PoolConfiguration{" +
                "id='" + id + '\'' +
                ", repositoryUrl='" + repositoryUrl + '\'' +
                ", maxConnections=" + maxConnections +
                '}';
    }
}
