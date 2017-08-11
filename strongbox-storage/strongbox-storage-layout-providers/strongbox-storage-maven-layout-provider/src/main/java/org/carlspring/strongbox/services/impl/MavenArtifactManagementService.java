package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.locator.handlers.RemoveTimestampedSnapshotOperation;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MavenSnapshotManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.providers.layout.LayoutProviderRegistry.getLayoutProvider;

/**
 * @author mtodorov
 */
@Component("mavenArtifactManagementService")
public class MavenArtifactManagementService
    extends AbstractArtifactManagementService
{

    private static final Logger logger = LoggerFactory.getLogger(MavenArtifactManagementService.class);

    @Inject
    private MavenSnapshotManager mavenSnapshotManager;
    
    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Override
    public boolean contains(String storageId, String repositoryId, String artifactPath)
            throws IOException
    {
        final Storage storage = getStorage(storageId);
        final Repository repository = storage.getRepository(repositoryId);

        try
        {
            LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);

            return layoutProvider.contains(storageId, repositoryId, artifactPath);
        }
        catch (IOException | ProviderImplementationException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public void copy(String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId,
                     String path)
            throws IOException
    {
        artifactOperationsValidator.validate(srcStorageId, srcRepositoryId, path);

        final Storage srcStorage = getStorage(srcStorageId);
        final Repository srcRepository = srcStorage.getRepository(srcRepositoryId);

        final Storage destStorage = getStorage(destStorageId);
        final Repository destRepository = destStorage.getRepository(destRepositoryId);

        File srcFile = new File(srcRepository.getBasedir(), path);
        File destFile = new File(destRepository.getBasedir(), path);

        if (srcFile.isDirectory())
        {
            FileUtils.copyDirectoryToDirectory(srcFile, destFile.getParentFile());
        }
        else
        {
            FileUtils.copyFile(srcFile, destFile);
        }
    }

    @Override
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
            LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
            RepositoryPath repositoryPath = layoutProvider.resolve(repository).resolve(artifactPath);
            
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

}
