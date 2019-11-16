package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.testing.storage.repository.RepositorySetup;
import org.carlspring.strongbox.yaml.configuration.repository.MavenRepositoryConfigurationDto;

public class MavenIndexedWithChecksumMetadataStrategyRepositorySetup implements RepositorySetup
{
    @Override
    public void setup(RepositoryDto repository)
    {
        MavenRepositoryConfigurationDto mavenRepositoryConfiguration = new MavenRepositoryConfigurationDto();
        mavenRepositoryConfiguration.setIndexingEnabled(true);
        mavenRepositoryConfiguration.setMetadataExpirationStrategy("checksum");
        repository.setRepositoryConfiguration(mavenRepositoryConfiguration);
    }

}
