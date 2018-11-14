package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class MavenRepositoryFactory implements RepositoryFactory
{
    @Inject
    private MavenRepositoryFeatures mavenRepositoryFeatures;

    @Override
    public MutableRepository createRepository(String repositoryId)
    {
        MutableRepository repository = new MutableRepository(repositoryId);
        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(mavenRepositoryFeatures.getDefaultArtifactCoordinateValidators());

        return repository;
    }

}
