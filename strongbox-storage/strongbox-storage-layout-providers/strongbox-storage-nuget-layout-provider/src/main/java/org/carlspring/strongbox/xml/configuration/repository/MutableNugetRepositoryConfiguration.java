package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.MutableCustomRepositoryConfiguration;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@JsonTypeName("nugetRepositoryConfiguration")
public class MutableNugetRepositoryConfiguration
        extends MutableCustomRepositoryConfiguration
{
    private String feedVersion = "2.0";

    private Integer remoteFeedPageSize;

    public String getFeedVersion()
    {
        return feedVersion;
    }

    public void setFeedVersion(String feedVersion)
    {
        this.feedVersion = feedVersion;
    }

    public Integer getRemoteFeedPageSize()
    {
        return remoteFeedPageSize;
    }

    public void setRemoteFeedPageSize(Integer remoteFeedPageSize)
    {
        this.remoteFeedPageSize = remoteFeedPageSize;
    }

    @Override
    public CustomRepositoryConfiguration getImmutable()
    {
        return new NugetRepositoryConfiguration(this);
    }
}
