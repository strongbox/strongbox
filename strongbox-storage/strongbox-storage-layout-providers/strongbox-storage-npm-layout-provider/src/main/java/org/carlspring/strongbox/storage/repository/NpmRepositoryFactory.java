package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
import org.carlspring.strongbox.repository.NpmRepositoryFeatures;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class NpmRepositoryFactory
        implements RepositoryFactory
{

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private NpmRepositoryFeatures npmRepositoryFeatures;


    @Override
    public Repository createRepository(String storageId, String repositoryId)
    {
        Repository repository = new Repository(repositoryId);
        repository.setStorage(getConfiguration().getStorage(storageId));
        repository.setLayout(NpmLayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(npmRepositoryFeatures.getDefaultArtifactCoordinateValidators());

        return repository;
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
