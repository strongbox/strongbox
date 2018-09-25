package org.carlspring.strongbox.xml.configuration.repository.remote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
import org.carlspring.strongbox.xml.repository.remote.CustomRemoteRepositoryConfiguration;
import org.glassfish.hk2.api.Immediate;

import com.fasterxml.jackson.annotation.JsonTypeName;

@Immediate
@JsonTypeName(NpmLayoutProvider.ALIAS)
@XmlAccessorType(XmlAccessType.FIELD)
public class NpmRemoteRepositoryConfiguration extends CustomRemoteRepositoryConfiguration
{

    private Long lastCnahgeId = 0L;

    NpmRemoteRepositoryConfiguration()
    {
    }

    NpmRemoteRepositoryConfiguration(MutableNpmRemoteRepositoryConfiguration delegate)
    {
        this.lastCnahgeId = delegate.getLastCnahgeId();
    }

    public Long getLastCnahgeId()
    {
        return lastCnahgeId;
    }

}
