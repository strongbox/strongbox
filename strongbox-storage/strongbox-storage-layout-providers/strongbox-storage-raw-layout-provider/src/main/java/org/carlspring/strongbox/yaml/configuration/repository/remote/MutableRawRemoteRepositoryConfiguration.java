package org.carlspring.strongbox.yaml.configuration.repository.remote;

import org.carlspring.strongbox.yaml.repository.remote.CustomRemoteRepositoryConfiguration;
import org.carlspring.strongbox.yaml.repository.remote.MutableRemoteRepositoryConfiguration;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Pablo Tirado
 */
@JsonTypeName("rawRemoteRepositoryConfiguration")
public class MutableRawRemoteRepositoryConfiguration extends MutableRemoteRepositoryConfiguration
{

    @Override
    public CustomRemoteRepositoryConfiguration getImmutable()
    {
        return new RawRemoteRepositoryConfiguration();
    }

}
