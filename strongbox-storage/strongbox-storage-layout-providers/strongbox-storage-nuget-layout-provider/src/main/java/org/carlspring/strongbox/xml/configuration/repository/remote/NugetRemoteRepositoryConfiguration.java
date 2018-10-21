package org.carlspring.strongbox.xml.configuration.repository.remote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.xml.repository.remote.CustomRemoteRepositoryConfiguration;
import org.glassfish.hk2.api.Immediate;

import com.fasterxml.jackson.annotation.JsonTypeName;

@Immediate
@JsonTypeName(NugetLayoutProvider.ALIAS)
@XmlAccessorType(XmlAccessType.FIELD)
public class NugetRemoteRepositoryConfiguration extends CustomRemoteRepositoryConfiguration
{

    NugetRemoteRepositoryConfiguration()
    {
    }

    NugetRemoteRepositoryConfiguration(MutableNugetRemoteRepositoryConfiguration delegate)
    {
    }

}
