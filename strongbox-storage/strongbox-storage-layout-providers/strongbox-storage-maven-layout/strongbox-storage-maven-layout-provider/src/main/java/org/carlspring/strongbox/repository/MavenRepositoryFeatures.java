package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
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
import org.carlspring.strongbox.yaml.configuration.repository.MavenRepositoryConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class MavenRepositoryFeatures
        implements RepositoryFeatures
{

    private static final Logger logger = LoggerFactory.getLogger(MavenRepositoryFeatures.class);

    public static final String INDEX = ".index";

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
        try
        {
            DateFormat formatter = new SimpleDateFormat(MavenSnapshotManager.TIMESTAMP_FORMAT);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -keepPeriod);
            Date keepDate = formatter.parse(formatter.format(calendar.getTime()));

            removeTimestampedSnapshots(storageId, repositoryId, artifactPath, numberToKeep, keepDate);
        }
        catch (ParseException e)
        {
            throw new IOException(e);
        }
    }

    public void removeTimestampedSnapshots(String storageId,
                                           String repositoryId,
                                           String artifactPath,
                                           int numberToKeep,
                                           Date keepDate)
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
            operation.setKeepDate(keepDate);

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

    public boolean isIndexingEnabled(Repository repository)
    {
        MavenRepositoryConfiguration repositoryConfiguration = (MavenRepositoryConfiguration) repository.getRepositoryConfiguration();
        return repositoryConfiguration != null && repositoryConfiguration.isIndexingEnabled();
    }
}
