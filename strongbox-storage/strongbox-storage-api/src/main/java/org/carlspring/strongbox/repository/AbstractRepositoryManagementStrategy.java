package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author carlspring
 */
public abstract class AbstractRepositoryManagementStrategy
        implements RepositoryManagementStrategy
{

    private static final Logger logger = LoggerFactory.getLogger(AbstractRepositoryManagementStrategy.class);

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Override
    public void createRepository(String storageId,
                                 String repositoryId)
            throws IOException, RepositoryManagementStrategyException
    {
        logger.debug(String.format("Creating repository [%s/%s]...", storageId, repositoryId));

        Storage storage = getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        createRepositoryStructure(repository);
        createRepositoryInternal(storage, getRepository(storageId, repositoryId));
    }

    @Override
    public void createRepositoryStructure(final Repository repository)
            throws IOException
    {
        final RepositoryPath rootRepositoryPath = repositoryPathResolver.resolve(repository);
        if (!Files.exists(rootRepositoryPath))
        {
            Files.createDirectories(rootRepositoryPath);
        }
        final RepositoryPath trashRepositoryPath = rootRepositoryPath.resolve(".trash");
        if (!Files.exists(trashRepositoryPath))
        {
            Files.createDirectories(trashRepositoryPath);
        }
    }

    protected void createRepositoryInternal(Storage storage,
                                            Repository repository)
            throws IOException, RepositoryManagementStrategyException
    {
        // override if needed
    }

    protected Storage getStorage(String storageId)
    {
        return getConfiguration().getStorage(storageId);
    }

    protected Repository getRepository(String storageId,
                                       String repositoryId)
    {
        return getStorage(storageId).getRepository(repositoryId);
    }

    @Override
    public void removeRepository(String storageId,
                                 String repositoryId)
            throws IOException
    {
        removeDirectoryStructure(storageId, repositoryId);
    }

    @Override
    public void removeDirectoryStructure(String storageId,
                                         String repositoryId)
            throws IOException
    {
        Storage storage = getStorage(storageId);

        final String storageBasedirPath = storage.getBasedir();

        final File repositoryBaseDir = new File(new File(storageBasedirPath), repositoryId);

        if (repositoryBaseDir.exists())
        {
            FileUtils.deleteDirectory(repositoryBaseDir);

            logger.debug("Removed directory structure for repository '" +
                         repositoryBaseDir.getAbsolutePath() + File.separatorChar + repositoryId + "'.");
        }
        else
        {
            throw new IOException("Failed to delete non-existing repository " +
                                  repositoryBaseDir.getAbsolutePath() + ".");
        }
    }


    protected Configuration getConfiguration()
    {
        return configurationManagementService.getConfiguration();
    }

}
