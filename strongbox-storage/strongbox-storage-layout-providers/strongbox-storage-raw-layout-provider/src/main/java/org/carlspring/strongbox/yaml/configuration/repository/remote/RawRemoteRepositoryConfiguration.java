package org.carlspring.strongbox.yaml.configuration.repository.remote;

import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.yaml.repository.remote.CustomRemoteRepositoryConfigurationData;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(RawLayoutProvider.ALIAS)
@XmlAccessorType(XmlAccessType.FIELD)
public class RawRemoteRepositoryConfiguration
        extends CustomRemoteRepositoryConfigurationData
{

    RawRemoteRepositoryConfiguration()
    {
    }

    RawRemoteRepositoryConfiguration(RawRemoteRepositoryConfigurationDto delegate)
    {
    }

}
