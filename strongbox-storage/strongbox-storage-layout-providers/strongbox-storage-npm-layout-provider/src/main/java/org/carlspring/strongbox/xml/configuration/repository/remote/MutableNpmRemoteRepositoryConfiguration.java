package org.carlspring.strongbox.xml.configuration.repository.remote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.carlspring.strongbox.xml.repository.remote.CustomRemoteRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.remote.MutableRemoteRepositoryConfiguration;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "npm-remote-repository-configuration")
public class MutableNpmRemoteRepositoryConfiguration extends MutableRemoteRepositoryConfiguration
{
    @XmlAttribute(name = "last-cnahge-id")
    private Long lastCnahgeId=0L;

    public MutableNpmRemoteRepositoryConfiguration()
    {
        super();
    }

    public Long getLastCnahgeId()
    {
        return lastCnahgeId;
    }

    public void setLastCnahgeId(Long lastCnahgeId)
    {
        this.lastCnahgeId = lastCnahgeId;
    }

    @Override
    public CustomRemoteRepositoryConfiguration getImmutable()
    {
        return new NpmRemoteRepositoryConfiguration(this);
    }

    
}
