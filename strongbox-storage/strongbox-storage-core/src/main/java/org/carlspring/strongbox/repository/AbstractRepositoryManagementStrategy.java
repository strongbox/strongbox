package org.carlspring.strongbox.repository;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
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


    @Override
    public void createRepository(String storageId,
                                 String repositoryId)
            throws IOException, RepositoryManagementStrategyException
    {
        logger.info(String.format("Create repository [%s/%s]", storageId, repositoryId));

        Storage storage = getStorage(storageId);
        createRepositoryStructure(storage.getBasedir(), repositoryId);
        createRepositoryInternal(storage, getRepository(storageId, repositoryId));
    }

    protected abstract void createRepositoryInternal(Storage storage, Repository repository)
            throws IOException, RepositoryManagementStrategyException;

    private Storage getStorage(String storageId)
    {
        Storage storage = getConfiguration().getStorage(storageId);
        return storage;
    }

    protected Repository getRepository(String storageId, String repositoryId)
    {
        return getStorage(storageId).getRepository(repositoryId);
    }

    protected File getRepositoryBaseDir(String storageId,
                                        String repositoryId)
    {
        return new File(getStorage(storageId).getBasedir(), repositoryId).getAbsoluteFile();
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

    public Configuration getConfiguration()
    {
        return configurationManagementService.getConfiguration();
    }

}
