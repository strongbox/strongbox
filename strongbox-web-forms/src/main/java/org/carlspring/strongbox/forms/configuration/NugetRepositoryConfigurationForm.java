package org.carlspring.strongbox.forms.configuration;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Przemyslaw Fusik
 */
@JsonTypeName("NuGet")
public class NugetRepositoryConfigurationForm
        extends CustomRepositoryConfigurationForm
{

    private String feedVersion;

    private Integer remoteFeedPageSize;

    public String getFeedVersion()
    {
        return feedVersion;
    }

    public void setFeedVersion(final String feedVersion)
    {
        this.feedVersion = feedVersion;
    }

    public Integer getRemoteFeedPageSize()
    {
        return remoteFeedPageSize;
    }

    public void setRemoteFeedPageSize(final Integer remoteFeedPageSize)
    {
        this.remoteFeedPageSize = remoteFeedPageSize;
    }

    @Override
    public <T> T accept(final CustomRepositoryConfigurationFormVisitor<T> visitor)
    {
        return visitor.visit(this);
    }
}
