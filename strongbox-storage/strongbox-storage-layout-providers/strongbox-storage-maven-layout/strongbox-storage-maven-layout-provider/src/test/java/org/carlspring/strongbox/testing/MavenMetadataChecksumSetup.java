package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.testing.storage.repository.RepositorySetup;
import org.carlspring.strongbox.yaml.configuration.repository.MavenRepositoryConfigurationDto;

import java.util.Collection;
import java.util.Collections;

/**
 * @author sbespalov
 */
public class MavenMetadataChecksumSetup implements RepositorySetup
{

    @Override
    public void setup(RepositoryDto repository)
    {
        MavenRepositoryConfigurationDto mavenRepositoryConfiguration = new MavenRepositoryConfigurationDto();
        mavenRepositoryConfiguration.setDigestAlgorithmSet(Collections.singleton("SHA-1"));
        repository.setRepositoryConfiguration(mavenRepositoryConfiguration);
    }
    
}
