package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.testing.storage.repository.RepositorySetup;
import org.carlspring.strongbox.yaml.configuration.repository.NugetRepositoryConfigurationDto;

/**
 * @author ankit.tomar
 */
public class NugetRemoteRepositorySetup implements RepositorySetup
{

    @Override
    public void setup(RepositoryDto repository)
    {

        NugetRepositoryConfigurationDto repositoryConfiguration = new NugetRepositoryConfigurationDto();
        repository.setRepositoryConfiguration(repositoryConfiguration);
    }
}
