package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class NugetRepositoryConfiguration
        extends CustomRepositoryConfiguration
{

    private final String feedVersion;

    private final Integer remoteFeedPageSize;


    public NugetRepositoryConfiguration(final MutableNugetRepositoryConfiguration delegate)
    {
        this.feedVersion = delegate.getFeedVersion();
        this.remoteFeedPageSize = delegate.getRemoteFeedPageSize();
    }

    public String getFeedVersion()
    {
        return feedVersion;
    }

    public Integer getRemoteFeedPageSize()
    {
        return remoteFeedPageSize;
    }
}
