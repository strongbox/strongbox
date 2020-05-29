package org.carlspring.strongbox.yaml.configuration.repository.remote;

import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.yaml.repository.remote.CustomRemoteRepositoryConfigurationData;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(NugetLayoutProvider.ALIAS)
@XmlAccessorType(XmlAccessType.FIELD)
public class NugetRemoteRepositoryConfiguration
        extends CustomRemoteRepositoryConfigurationData
{

    NugetRemoteRepositoryConfiguration()
    {
    }

    NugetRemoteRepositoryConfiguration(NugetRemoteRepositoryConfigurationDto delegate)
    {
    }

}
