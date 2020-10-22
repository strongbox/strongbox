package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepositoryDto;
import org.carlspring.strongbox.testing.storage.repository.RepositorySetup;
import org.carlspring.strongbox.yaml.configuration.repository.remote.NpmRemoteRepositoryConfigurationDto;

/**
 * @author Pablo Tirado
 */
public class NpmReplicateUrlRepositorySetup
        implements RepositorySetup
{

    private static final String REPLICATE_URL = "https://replicate.npmjs.com";

    @Override
    public void setup(RepositoryDto repository)
    {
        RemoteRepositoryDto remoteRepository = new RemoteRepositoryDto();

        NpmRemoteRepositoryConfigurationDto remoteRepositoryConfiguration = new NpmRemoteRepositoryConfigurationDto();
        remoteRepositoryConfiguration.setReplicateUrl(REPLICATE_URL);

        remoteRepository.setCustomConfiguration(remoteRepositoryConfiguration);

        repository.setRemoteRepository(remoteRepository);
    }
    
}
