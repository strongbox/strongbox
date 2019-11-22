package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.providers.layout.RpmLayoutProvider;
import org.carlspring.strongbox.repository.RpmRepositoryFeatures;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.LinkedHashSet;

/**q
 * @author carlspring
 */
@Component
public class RpmRepositoryFactory
        implements RepositoryFactory
{

    @Inject
    private RpmRepositoryFeatures rpmRepositoryFeatures;


    @Override
    public RepositoryDto createRepository(String repositoryId)
    {
        RepositoryDto repository = new RepositoryDto(repositoryId);
        repository.setLayout(RpmLayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(new LinkedHashSet<>(rpmRepositoryFeatures.getDefaultArtifactCoordinateValidators()));

        return repository;
    }

}
