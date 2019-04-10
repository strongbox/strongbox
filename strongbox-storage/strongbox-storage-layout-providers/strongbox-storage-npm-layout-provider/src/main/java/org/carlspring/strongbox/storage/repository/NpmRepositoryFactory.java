package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
import org.carlspring.strongbox.repository.NpmRepositoryFeatures;

import javax.inject.Inject;
import java.util.LinkedHashSet;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class NpmRepositoryFactory
        implements RepositoryFactory
{

    @Inject
    private NpmRepositoryFeatures npmRepositoryFeatures;


    @Override
    public MutableRepository createRepository(String repositoryId)
    {
        MutableRepository repository = new MutableRepository(repositoryId);
        repository.setLayout(NpmLayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(
                new LinkedHashSet<>(npmRepositoryFeatures.getDefaultArtifactCoordinateValidators()));

        return repository;
    }

}
