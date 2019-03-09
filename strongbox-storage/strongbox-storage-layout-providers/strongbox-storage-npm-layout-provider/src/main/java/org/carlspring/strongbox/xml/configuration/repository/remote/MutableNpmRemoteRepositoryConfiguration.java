package org.carlspring.strongbox.xml.configuration.repository.remote;

import org.carlspring.strongbox.xml.repository.remote.CustomRemoteRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.remote.MutableRemoteRepositoryConfiguration;

public class MutableNpmRemoteRepositoryConfiguration extends MutableRemoteRepositoryConfiguration
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
    public CustomRemoteRepositoryConfiguration getImmutable()
    {
        return new NpmRemoteRepositoryConfiguration(this);
    }

}
