package org.carlspring.strongbox.yaml.configuration.repository;

import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfigurationDto;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@JsonTypeName(NugetLayoutProvider.ALIAS)
public class NugetRepositoryConfigurationDto
        extends CustomRepositoryConfigurationDto implements NugetRepositoryConfiguration
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
        return new NugetRepositoryConfigurationData(this);
    }
}
