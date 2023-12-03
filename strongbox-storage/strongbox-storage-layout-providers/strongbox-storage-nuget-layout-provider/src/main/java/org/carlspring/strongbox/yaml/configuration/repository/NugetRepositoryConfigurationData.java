package org.carlspring.strongbox.yaml.configuration.repository;

import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfiguration;

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
public class NugetRepositoryConfigurationData
        extends CustomRepositoryConfiguration implements NugetRepositoryConfiguration
{

    private String feedVersion;

    private Integer remoteFeedPageSize;

    public NugetRepositoryConfigurationData()
    {

    }


    public NugetRepositoryConfigurationData(final NugetRepositoryConfigurationDto delegate)
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
