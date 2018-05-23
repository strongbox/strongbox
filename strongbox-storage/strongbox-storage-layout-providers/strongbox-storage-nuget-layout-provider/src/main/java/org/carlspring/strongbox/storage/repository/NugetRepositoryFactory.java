package org.carlspring.strongbox.storage.repository;

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
    private NugetRepositoryFeatures nugetRepositoryFeatures;


    @Override
    public MutableRepository createRepository(String repositoryId)
    {
        MutableRepository repository = new MutableRepository(repositoryId);
        repository.setLayout(NugetLayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(nugetRepositoryFeatures.getDefaultArtifactCoordinateValidators());

        return repository;
    }

}
