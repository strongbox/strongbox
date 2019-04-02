package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressFBWarnings(value = "AJCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
@JsonTypeName(NugetLayoutProvider.ALIAS)
public class ImmutableNugetRepositoryConfiguration
        extends CustomRepositoryConfiguration implements NugetRepositoryConfiguration
{

    private String feedVersion;

    private Integer remoteFeedPageSize;

    ImmutableNugetRepositoryConfiguration()
    {

    }


    public ImmutableNugetRepositoryConfiguration(final MutableNugetRepositoryConfiguration delegate)
    {
        this.feedVersion = delegate.getFeedVersion();
        this.remoteFeedPageSize = delegate.getRemoteFeedPageSize();
    }

    @Override
    public String getFeedVersion()
    {
        return feedVersion;
    }

    @Override
    public Integer getRemoteFeedPageSize()
    {
        return remoteFeedPageSize;
    }
}
