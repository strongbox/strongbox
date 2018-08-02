package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@JsonTypeName(NugetLayoutProvider.ALIAS)
public class NugetRepositoryConfiguration
        extends CustomRepositoryConfiguration
{

    private String feedVersion;

    private Integer remoteFeedPageSize;

    NugetRepositoryConfiguration()
    {

    }


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
