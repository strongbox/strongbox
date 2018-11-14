package org.carlspring.strongbox.xml.configuration.repository.remote;

import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.xml.repository.remote.CustomRemoteRepositoryConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.glassfish.hk2.api.Immediate;

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
