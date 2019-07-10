package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.repository.NugetRepositoryFeatures;

import javax.inject.Inject;
import java.util.LinkedHashSet;

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
    public RepositoryDto createRepository(String repositoryId)
    {
        RepositoryDto repository = new RepositoryDto(repositoryId);
        repository.setLayout(NugetLayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(
                new LinkedHashSet<>(nugetRepositoryFeatures.getDefaultArtifactCoordinateValidators()));

        return repository;
    }

}
