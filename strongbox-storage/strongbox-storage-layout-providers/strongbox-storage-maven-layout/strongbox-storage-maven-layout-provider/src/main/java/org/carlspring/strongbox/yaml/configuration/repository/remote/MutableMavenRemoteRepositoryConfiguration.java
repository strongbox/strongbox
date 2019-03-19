package org.carlspring.strongbox.yaml.configuration.repository.remote;

import org.carlspring.strongbox.yaml.repository.remote.CustomRemoteRepositoryConfiguration;
import org.carlspring.strongbox.yaml.repository.remote.MutableRemoteRepositoryConfiguration;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Pablo Tirado
 */
@JsonTypeName("mavenRemoteRepositoryConfiguration")
public class MutableMavenRemoteRepositoryConfiguration extends MutableRemoteRepositoryConfiguration
{

    @Override
    public CustomRemoteRepositoryConfiguration getImmutable()
    {
        return new MavenRemoteRepositoryConfiguration();
    }

}
