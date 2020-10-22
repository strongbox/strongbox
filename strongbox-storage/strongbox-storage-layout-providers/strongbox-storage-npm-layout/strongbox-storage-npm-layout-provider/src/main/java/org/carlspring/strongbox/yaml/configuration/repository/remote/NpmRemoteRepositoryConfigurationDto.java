package org.carlspring.strongbox.yaml.configuration.repository.remote;

import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
import org.carlspring.strongbox.yaml.repository.remote.CustomRemoteRepositoryConfigurationData;
import org.carlspring.strongbox.yaml.repository.remote.RemoteRepositoryConfigurationDto;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Pablo Tirado
 */
@JsonTypeName(NpmLayoutProvider.ALIAS)
public class NpmRemoteRepositoryConfigurationDto
        extends RemoteRepositoryConfigurationDto
{

    private Long lastChangeId = 0L;

    private String replicateUrl;

    public Long getLastChangeId()
    {
        return lastChangeId;
    }

    public void setLastChangeId(Long lastChangeId)
    {
        this.lastChangeId = lastChangeId;
    }

    public String getReplicateUrl()
    {
        return replicateUrl;
    }

    public void setReplicateUrl(String replicateUrl)
    {
        this.replicateUrl = replicateUrl;
    }

    @Override
    public CustomRemoteRepositoryConfigurationData getImmutable()
    {
        return new NpmRemoteRepositoryConfiguration(this);
    }

}
