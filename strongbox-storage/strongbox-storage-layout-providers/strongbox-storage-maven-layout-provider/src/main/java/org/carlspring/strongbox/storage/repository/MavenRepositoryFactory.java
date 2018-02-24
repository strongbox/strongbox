package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class MavenRepositoryFactory implements RepositoryFactory
{

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private MavenRepositoryFeatures mavenRepositoryFeatures;


    @Override
    public Repository createRepository(String storageId, String repositoryId)
    {
        Repository repository = new Repository(repositoryId);
        repository.setStorage(getConfiguration().getStorage(storageId));
        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(mavenRepositoryFeatures.getDefaultArtifactCoordinateValidators());

        return repository;
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
