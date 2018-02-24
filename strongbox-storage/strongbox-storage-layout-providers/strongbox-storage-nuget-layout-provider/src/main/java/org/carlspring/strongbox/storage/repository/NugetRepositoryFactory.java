package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.repository.NugetRepositoryFeatures;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class NugetRepositoryFactory
        implements RepositoryFactory
{

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private NugetRepositoryFeatures nugetRepositoryFeatures;


    @Override
    public Repository createRepository(String storageId, String repositoryId)
    {
        Repository repository = new Repository(repositoryId);
        repository.setStorage(getConfiguration().getStorage(storageId));
        repository.setLayout(NugetLayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(nugetRepositoryFeatures.getDefaultArtifactCoordinateValidators());

        return repository;
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
