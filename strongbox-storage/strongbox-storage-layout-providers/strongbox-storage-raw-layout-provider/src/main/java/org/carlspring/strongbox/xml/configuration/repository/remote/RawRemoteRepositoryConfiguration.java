package org.carlspring.strongbox.xml.configuration.repository.remote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.xml.repository.remote.CustomRemoteRepositoryConfiguration;
import org.glassfish.hk2.api.Immediate;

import com.fasterxml.jackson.annotation.JsonTypeName;

@Immediate
@JsonTypeName(RawLayoutProvider.ALIAS)
@XmlAccessorType(XmlAccessType.FIELD)
public class RawRemoteRepositoryConfiguration extends CustomRemoteRepositoryConfiguration
{

    RawRemoteRepositoryConfiguration()
    {
    }

    RawRemoteRepositoryConfiguration(MutableRawRemoteRepositoryConfiguration delegate)
    {
    }

}
