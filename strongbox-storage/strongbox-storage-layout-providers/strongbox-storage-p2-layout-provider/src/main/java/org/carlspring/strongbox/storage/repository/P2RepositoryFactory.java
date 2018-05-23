package org.carlspring.strongbox.storage.repository;

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
    private P2RepositoryFeatures p2RepositoryFeatures;


    @Override
    public MutableRepository createRepository(String repositoryId)
    {
        MutableRepository repository = new MutableRepository(repositoryId);
        repository.setLayout(P2LayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(p2RepositoryFeatures.getDefaultArtifactCoordinateValidators());

        return repository;
    }

}
