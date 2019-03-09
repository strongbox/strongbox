package org.carlspring.strongbox.xml.configuration.repository.remote;

import org.carlspring.strongbox.xml.repository.remote.CustomRemoteRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.remote.MutableRemoteRepositoryConfiguration;

public class MutableMavenRemoteRepositoryConfiguration extends MutableRemoteRepositoryConfiguration
{

    @Override
    public CustomRemoteRepositoryConfiguration getImmutable()
    {
        return new MavenRemoteRepositoryConfiguration();
    }

}
