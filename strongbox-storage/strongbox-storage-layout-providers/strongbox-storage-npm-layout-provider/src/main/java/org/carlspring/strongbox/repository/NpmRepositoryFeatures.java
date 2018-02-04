package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.artifact.version.GenericReleaseVersionValidator;
import org.carlspring.strongbox.storage.validation.artifact.version.GenericSnapshotVersionValidator;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class NpmRepositoryFeatures implements RepositoryFeatures
{

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private GenericReleaseVersionValidator genericReleaseVersionValidator;

    @Inject
    private GenericSnapshotVersionValidator genericSnapshotVersionValidator;


    @Override
    public Repository createRepositoryInstance(String storageId, String repositoryId)
    {
        Repository repository = new Repository(repositoryId);
        repository.setStorage(configurationManager.getConfiguration().getStorage(storageId));
        repository.setLayout(NpmLayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(getDefaultArtifactCoordinateValidators());

        return repository;
    }

    @Override
    public Set<String> getDefaultArtifactCoordinateValidators()
    {
        Set<String> validators = new LinkedHashSet<>();
        validators.add(genericReleaseVersionValidator.getAlias());
        validators.add(genericSnapshotVersionValidator.getAlias());

        return validators;
    }

}
