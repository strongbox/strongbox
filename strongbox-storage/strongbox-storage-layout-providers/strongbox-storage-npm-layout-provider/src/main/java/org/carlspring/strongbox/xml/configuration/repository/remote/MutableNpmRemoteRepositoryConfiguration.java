package org.carlspring.strongbox.xml.configuration.repository.remote;

import org.carlspring.strongbox.xml.repository.remote.CustomRemoteRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.remote.MutableRemoteRepositoryConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "npm-remote-repository-configuration")
public class MutableNpmRemoteRepositoryConfiguration extends MutableRemoteRepositoryConfiguration
{
    @XmlAttribute(name = "last-change-id")
    private Long lastChangeId = 0L;

    @XmlAttribute(name = "replicate-url")
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
