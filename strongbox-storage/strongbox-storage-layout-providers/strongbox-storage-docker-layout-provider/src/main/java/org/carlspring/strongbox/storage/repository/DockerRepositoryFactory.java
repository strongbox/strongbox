package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.providers.layout.DockerLayoutProvider;
import org.carlspring.strongbox.repository.DockerRepositoryFeatures;

import javax.inject.Inject;
import java.util.LinkedHashSet;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class DockerRepositoryFactory
        implements RepositoryFactory
{

    @Inject
    private DockerRepositoryFeatures dockerRepositoryFeatures;


    @Override
    public MutableRepository createRepository(String repositoryId)
    {
        MutableRepository repository = new MutableRepository(repositoryId);
        repository.setLayout(DockerLayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(new LinkedHashSet<>(dockerRepositoryFeatures.getDefaultArtifactCoordinateValidators()));

        return repository;
    }

}
