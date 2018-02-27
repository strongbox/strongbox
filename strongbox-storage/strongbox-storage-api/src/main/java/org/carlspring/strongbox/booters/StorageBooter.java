package org.carlspring.strongbox.booters;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.repository.group.GroupRepositorySetCollector;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryStatusEnum;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Singleton
@Component("storageBooter")
@Scope("singleton")
public class StorageBooter
{

    private static final Logger logger = LoggerFactory.getLogger(StorageBooter.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    private GroupRepositorySetCollector groupRepositorySetCollector;

    private File lockFile = new File(ConfigurationResourceResolver.getVaultDirectory(), "storage-booter.lock");


    public StorageBooter()
    {
    }

    @PostConstruct
    public void initialize()
            throws IOException, RepositoryManagementStrategyException
    {
        if (!lockExists())
        {
            createLockFile();
            createTempDir();

            initializeStorages();

            Collection<Repository> repositories = getRepositoriesHierarchy();

            for (Repository repository : repositories)
            {
                try
                {
                    initializeRepository(repository);
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Failed to initialize the repository '" + repository + "'.");
                }
            }
        }
        else
        {
            logger.debug("Failed to initialize the repositories. Another JVM may have already done this.");
        }
    }

    @PreDestroy
    public void removeLock()
    {
        //noinspection ResultOfMethodCallIgnored
        lockFile.delete();

        logger.debug("Removed lock file '" + lockFile.getAbsolutePath() + "'.");
    }

    public void createTempDir() throws IOException
    {
        String tempDirLocation = System.getProperty("java.io.tmpdir",
                                                Paths.get(ConfigurationResourceResolver.getVaultDirectory(), "tmp")
                                                     .toAbsolutePath()
                                                     .toString());
        Path tempDirPath = Paths.get(tempDirLocation).toAbsolutePath();
        if (!Files.exists(tempDirPath))
        {
            Files.createDirectories(tempDirPath);
        }

        logger.debug("Temporary directory: " + tempDirPath.toString() + ".");

        if (System.getProperty("java.io.tmpdir") == null)
        {
            System.setProperty("java.io.tmpdir", tempDirPath.toString());

            logger.debug("Set java.io.tmpdir to " + tempDirPath.toString() + ".");
        }
        else
        {
            logger.debug("The java.io.tmpdir is already set to " + System.getProperty("java.io.tmpdir") + ".");
        }
    }

    private void createLockFile()
            throws IOException
    {
        //noinspection ResultOfMethodCallIgnored
        lockFile.getParentFile().mkdirs();
        //noinspection ResultOfMethodCallIgnored
        lockFile.createNewFile();

        logger.debug(" -> Created lock file '" + lockFile.getAbsolutePath() + "'...");
    }

    private boolean lockExists()
            throws IOException
    {
        if (lockFile.exists())
        {
            logger.debug(" -> Lock found: '" + ConfigurationResourceResolver.getVaultDirectory() + "'!");

            return true;
        }
        else
        {
            logger.debug(" -> No lock found.");

            return false;
        }
    }

    /**
     * @return The base directory for the storages
     */
    private File initializeStorages()
            throws IOException
    {
        logger.debug("Running Strongbox storage booter...");
        logger.debug(" -> Creating storage directory skeleton...");

        String basedir;
        if (System.getProperty("strongbox.storage.booter.basedir") != null)
        {
            basedir = System.getProperty("strongbox.storage.booter.basedir");
        }
        else
        {
            // Assuming this invocation is related to tests:
            basedir = ConfigurationResourceResolver.getVaultDirectory() + "/storages";
        }

        final Map<String, Storage> storageEntry = configurationManager.getConfiguration().getStorages();
        for (Map.Entry<String, Storage> stringStorageEntry : storageEntry.entrySet())
        {
            initializeStorage(stringStorageEntry.getValue());
        }

        return new File(basedir).getAbsoluteFile();
    }

    private File initializeStorage(Storage storage)
            throws IOException
    {
        File storagesBaseDir = new File(storage.getBasedir());
        if (!storagesBaseDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            storagesBaseDir.mkdirs();
        }

        return storagesBaseDir;
    }

    private void initializeRepository(Repository repository)
            throws IOException, RepositoryManagementStrategyException
    {
        final File repositoryBasedir = new File(repository.getStorage().getBasedir(), repository.getId());

        logger.debug("  * Initializing '" + repositoryBasedir.getAbsolutePath() + "'...");

        repositoryManagementService.createRepository(repository.getStorage().getId(), repository.getId());

        if (RepositoryStatusEnum.IN_SERVICE.getStatus().equals(repository.getStatus()))
        {
            repositoryManagementService.putInService(repository.getStorage().getId(), repository.getId());
        }
    }

    private Collection<Repository> getRepositoriesHierarchy()
    {
        final Map<String, Repository> repositoriesHierarchy = new LinkedHashMap<>();
        final Configuration configuration = configurationManager.getConfiguration();
        for (final Storage storage : configuration.getStorages().values())
        {
            for (final Repository repository : storage.getRepositories().values())
            {
                addRepositoriesByChildrenFirst(repositoriesHierarchy, repository);
            }
        }
        return repositoriesHierarchy.values();
    }

    private void addRepositoriesByChildrenFirst(final Map<String, Repository> repositoriesHierarchy,
                                                final Repository repository)
    {
        if (!repository.isGroupRepository())
        {
            repositoriesHierarchy.putIfAbsent(repository.getId(), repository);
            return;
        }
        groupRepositorySetCollector.collect(repository, true).stream().forEach(
                r -> addRepositoriesByChildrenFirst(repositoriesHierarchy, r));
        repositoriesHierarchy.putIfAbsent(repository.getId(), repository);
    }

    public RepositoryManagementService getRepositoryManagementService()
    {
        return repositoryManagementService;
    }

    public void setRepositoryManagementService(RepositoryManagementService repositoryManagementService)
    {
        this.repositoryManagementService = repositoryManagementService;
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
