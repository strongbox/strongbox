package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositorySetup;
import org.carlspring.strongbox.yaml.configuration.repository.MutableMavenRepositoryConfiguration;

/**
 * @author sbespalov
 */
public class MavenIndexedRepositorySetup implements RepositorySetup
{

    @Override
    public void setup(MutableRepository repository)
    {
        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(true);
        repository.setRepositoryConfiguration(mavenRepositoryConfiguration);
    }
    
}
