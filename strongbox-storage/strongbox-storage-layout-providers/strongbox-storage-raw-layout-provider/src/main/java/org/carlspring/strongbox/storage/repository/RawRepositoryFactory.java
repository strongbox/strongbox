package org.carlspring.strongbox.storage.repository;

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
    private RawRepositoryFeatures rawRepositoryFeatures;


    @Override
    public MutableRepository createRepository(String repositoryId)
    {
        MutableRepository repository = new MutableRepository(repositoryId);
        repository.setLayout(RawLayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(rawRepositoryFeatures.getDefaultArtifactCoordinateValidators());

        return repository;
    }

}
