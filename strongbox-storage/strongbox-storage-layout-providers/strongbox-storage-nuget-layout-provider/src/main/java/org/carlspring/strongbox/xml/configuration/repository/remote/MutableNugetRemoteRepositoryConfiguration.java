package org.carlspring.strongbox.xml.configuration.repository.remote;

import org.carlspring.strongbox.xml.repository.remote.CustomRemoteRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.remote.MutableRemoteRepositoryConfiguration;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * @author Pablo Tirado
 */
@JsonRootName("nugetRemoteRepositoryConfiguration")
public class MutableNugetRemoteRepositoryConfiguration extends MutableRemoteRepositoryConfiguration
{

    @Override
    public CustomRemoteRepositoryConfiguration getImmutable()
    {
        return new NugetRemoteRepositoryConfiguration();
    }

}
