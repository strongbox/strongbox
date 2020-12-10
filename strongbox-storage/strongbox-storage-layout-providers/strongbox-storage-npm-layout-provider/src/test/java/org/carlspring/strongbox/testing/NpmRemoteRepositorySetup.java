package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.testing.storage.repository.RepositorySetup;
import org.carlspring.strongbox.yaml.configuration.repository.NpmRepositoryConfigurationDto;

/**
 * @author ankit.tomar
 */
public class NpmRemoteRepositorySetup implements RepositorySetup
{

    @Override
    public void setup(RepositoryDto repository)
    {

        NpmRepositoryConfigurationDto repositoryConfiguration = new NpmRepositoryConfigurationDto();
        repository.setRepositoryConfiguration(repositoryConfiguration);
    }

}
