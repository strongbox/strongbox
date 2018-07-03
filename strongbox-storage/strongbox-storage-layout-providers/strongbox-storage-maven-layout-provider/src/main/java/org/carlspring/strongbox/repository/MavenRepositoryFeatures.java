package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.config.MavenIndexerDisabledCondition;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.locator.handlers.RemoveTimestampedSnapshotOperation;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MavenSnapshotManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.validation.deployment.RedeploymentValidator;
import org.carlspring.strongbox.storage.validation.version.MavenReleaseVersionValidator;
import org.carlspring.strongbox.storage.validation.version.MavenSnapshotVersionValidator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
@Conditional(MavenIndexerDisabledCondition.class)
public class MavenRepositoryFeatures
        implements RepositoryFeatures
{

    private static final Logger logger = LoggerFactory.getLogger(MavenRepositoryFeatures.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private MavenSnapshotManager mavenSnapshotManager;

    @Inject
    private RedeploymentValidator redeploymentValidator;

    @Inject
    private MavenReleaseVersionValidator mavenReleaseVersionValidator;

    @Inject
    private MavenSnapshotVersionValidator mavenSnapshotVersionValidator;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    private Set<String> defaultArtifactCoordinateValidators;


    @PostConstruct
    public void init()
    {
        defaultArtifactCoordinateValidators = new LinkedHashSet<>(Arrays.asList(redeploymentValidator.getAlias(),
                                                                                mavenReleaseVersionValidator.getAlias(),
                                                                                mavenSnapshotVersionValidator.getAlias()));
    }

    public void removeTimestampedSnapshots(String storageId,
                                           String repositoryId,
                                           String artifactPath,
                                           int numberToKeep,
                                           int keepPeriod)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        if (repository.getPolicy().equals(RepositoryPolicyEnum.SNAPSHOT.getPolicy()))
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, artifactPath);

            RemoveTimestampedSnapshotOperation operation = new RemoveTimestampedSnapshotOperation(mavenSnapshotManager);
            operation.setBasePath(repositoryPath);
            operation.setNumberToKeep(numberToKeep);
            operation.setKeepPeriod(keepPeriod);

            ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
            locator.setOperation(operation);
            locator.locateArtifactDirectories();
        }
        else
        {
            throw new ArtifactStorageException("Type of repository is invalid: repositoryId - " + repositoryId);
        }
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    @Override
    public Set<String> getDefaultArtifactCoordinateValidators()
    {
        return defaultArtifactCoordinateValidators;
    }

}
