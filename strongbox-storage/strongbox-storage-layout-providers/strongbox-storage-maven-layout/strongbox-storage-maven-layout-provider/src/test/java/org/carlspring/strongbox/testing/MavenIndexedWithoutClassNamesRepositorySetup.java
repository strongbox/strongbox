package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.testing.storage.repository.RepositorySetup;
import org.carlspring.strongbox.yaml.configuration.repository.MavenRepositoryConfigurationDto;

/**
 * @author Pablo Tirado
 */
public class MavenIndexedWithoutClassNamesRepositorySetup
        implements RepositorySetup
{

    @Override
    public void setup(RepositoryDto repository)
    {
        MavenRepositoryConfigurationDto mavenRepositoryConfiguration = new MavenRepositoryConfigurationDto();
        mavenRepositoryConfiguration.setIndexingEnabled(true);
        mavenRepositoryConfiguration.setIndexingClassNamesEnabled(false);
        repository.setRepositoryConfiguration(mavenRepositoryConfiguration);
    }

}
