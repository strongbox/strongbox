package org.carlspring.strongbox.repository.group;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.locator.handlers.GenerateMavenMetadataOperation;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
public abstract class BaseMavenGroupRepositoryComponentTest
{

    protected static final String STORAGE0 = "storage0";

    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    @Inject
    protected ConfigurationManagementService configurationManagementService;

    @Inject
    protected MavenMetadataManager mavenMetadataManager;

    @Inject
    protected ArtifactEventListenerRegistry artifactEventListenerRegistry;

    protected void copyArtifactMetadata(String sourceRepositoryId,
                                        String destinationRepositoryId,
                                        String path)
            throws IOException
    {
        final Storage storage = getConfiguration().getStorage(STORAGE0);

        Repository repository = storage.getRepository(sourceRepositoryId);
        final Path sourcePath = repositoryPathResolver.resolve(repository, path);

        repository = storage.getRepository(destinationRepositoryId);
        final Path destinationPath = repositoryPathResolver.resolve(repository, path);
        FileUtils.copyFile(sourcePath.toFile(), destinationPath.toFile());
    }

    protected void generateMavenMetadata(Repository repository)
            throws IOException
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

        ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
        locator.setBasedir(repositoryPath);
        locator.setOperation(new GenerateMavenMetadataOperation(mavenMetadataManager, artifactEventListenerRegistry));
        locator.locateArtifactDirectories();
    }

    protected Configuration getConfiguration()
    {
        return configurationManagementService.getConfiguration();
    }

}
