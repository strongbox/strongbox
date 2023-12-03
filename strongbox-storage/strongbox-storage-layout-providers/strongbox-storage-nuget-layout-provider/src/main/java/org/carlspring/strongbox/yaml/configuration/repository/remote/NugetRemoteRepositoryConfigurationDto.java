package org.carlspring.strongbox.yaml.configuration.repository.remote;

import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.yaml.repository.remote.CustomRemoteRepositoryConfigurationData;
import org.carlspring.strongbox.yaml.repository.remote.RemoteRepositoryConfigurationDto;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Pablo Tirado
 */
@JsonTypeName(NugetLayoutProvider.ALIAS)
public class NugetRemoteRepositoryConfigurationDto
        extends RemoteRepositoryConfigurationDto
{

    @Override
    public CustomRemoteRepositoryConfigurationData getImmutable()
    {
        return new NugetRemoteRepositoryConfiguration();
    }

}
