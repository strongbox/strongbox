package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.providers.layout.P2LayoutProvider;
import org.carlspring.strongbox.repository.P2RepositoryFeatures;

import javax.inject.Inject;
import java.util.LinkedHashSet;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class P2RepositoryFactory
        implements RepositoryFactory
{

    @Inject
    private P2RepositoryFeatures p2RepositoryFeatures;


    @Override
    public RepositoryDto createRepository(String repositoryId)
    {
        RepositoryDto repository = new RepositoryDto(repositoryId);
        repository.setLayout(P2LayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(
                new LinkedHashSet<>(p2RepositoryFeatures.getDefaultArtifactCoordinateValidators()));

        return repository;
    }

}
