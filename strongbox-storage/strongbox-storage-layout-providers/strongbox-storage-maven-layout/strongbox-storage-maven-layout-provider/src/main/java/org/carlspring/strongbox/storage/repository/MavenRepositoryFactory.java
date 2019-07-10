package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;

import javax.inject.Inject;
import java.util.LinkedHashSet;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class MavenRepositoryFactory
        implements RepositoryFactory
{

    @Inject
    private MavenRepositoryFeatures mavenRepositoryFeatures;

    @Override
    public RepositoryDto createRepository(String repositoryId)
    {
        RepositoryDto repository = new RepositoryDto(repositoryId);
        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(
                new LinkedHashSet<>(mavenRepositoryFeatures.getDefaultArtifactCoordinateValidators()));

        return repository;
    }

}
