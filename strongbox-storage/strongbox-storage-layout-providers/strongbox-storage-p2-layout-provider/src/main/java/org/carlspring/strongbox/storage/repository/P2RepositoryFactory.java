package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.P2LayoutProvider;
import org.carlspring.strongbox.repository.P2RepositoryFeatures;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class P2RepositoryFactory
        implements RepositoryFactory
{

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private P2RepositoryFeatures p2RepositoryFeatures;


    @Override
    public Repository createRepository(String storageId, String repositoryId)
    {
        Repository repository = new Repository(repositoryId);
        repository.setStorage(getConfiguration().getStorage(storageId));
        repository.setLayout(P2LayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(p2RepositoryFeatures.getDefaultArtifactCoordinateValidators());

        return repository;
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
