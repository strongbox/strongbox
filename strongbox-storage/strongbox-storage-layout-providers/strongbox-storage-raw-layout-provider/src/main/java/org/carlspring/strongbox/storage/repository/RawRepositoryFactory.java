package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.repository.RawRepositoryFeatures;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class RawRepositoryFactory
        implements RepositoryFactory
{

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private RawRepositoryFeatures rawRepositoryFeatures;


    @Override
    public Repository createRepository(String storageId, String repositoryId)
    {
        Repository repository = new Repository(repositoryId);
        repository.setStorage(getConfiguration().getStorage(storageId));
        repository.setLayout(RawLayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(rawRepositoryFeatures.getDefaultArtifactCoordinateValidators());

        return repository;
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
