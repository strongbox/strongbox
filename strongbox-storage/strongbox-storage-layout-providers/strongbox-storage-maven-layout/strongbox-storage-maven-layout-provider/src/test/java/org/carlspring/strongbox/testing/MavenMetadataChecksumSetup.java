package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.testing.storage.repository.RepositorySetup;
import org.carlspring.strongbox.yaml.configuration.repository.MavenRepositoryConfigurationDto;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author sainalshah
 */
public class MavenMetadataChecksumSetup implements RepositorySetup
{

    @Override
    public void setup(RepositoryDto repository)
    {
        MavenRepositoryConfigurationDto mavenRepositoryConfiguration = new MavenRepositoryConfigurationDto();
        Set<String> digestAlgorithmSet = new HashSet<>();
        digestAlgorithmSet.add("SHA-256");
        digestAlgorithmSet.add("SHA-512");
        mavenRepositoryConfiguration.setDigestAlgorithmSet(digestAlgorithmSet);
        repository.setRepositoryConfiguration(mavenRepositoryConfiguration);
    }
}
