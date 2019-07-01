package org.carlspring.strongbox.booters;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.repository.group.GroupRepositorySetCollector;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.StorageData;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.storage.repository.RepositoryStatusEnum;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
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

    @Inject
    private PropertiesBooter propertiesBooter;

    private Path lockFile;


    public StorageBooter()
    {
    }

    @PostConstruct
    public void initialize()
            throws IOException, RepositoryManagementStrategyException
    {
        lockFile = Paths.get(propertiesBooter.getVaultDirectory()).resolve("storage-booter.lock");

        if (!lockExists())
        {
            createLockFile();
            
            final Configuration configuration = configurationManager.getConfiguration();

            initializeStorages(configuration.getStorages());

            Collection<RepositoryData> repositories = getRepositoriesHierarchy(configuration.getStorages());

            if (!repositories.isEmpty())
            {
                logger.info(" -> Initializing repositories...");
            }

            for (RepositoryData repository : repositories)
            {
                try
                {
                    initializeRepository(repository);
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Failed to initialize the repository '" + repository + "'.", e);
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
            throws IOException
    {
        Files.deleteIfExists(lockFile);

        logger.debug("Removed lock file '" + lockFile.toAbsolutePath().toString() + "'.");
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private void createLockFile()
            throws IOException
    {
        Files.createDirectories(lockFile.getParent());
        Files.createFile(lockFile);

        logger.debug(" -> Created lock file '" + lockFile.toAbsolutePath().toString() + "'...");
    }

    private boolean lockExists()
    {
        if (Files.exists(lockFile))
        {
            logger.debug(" -> Lock found: '" + propertiesBooter.getVaultDirectory() + "'!");

            return true;
        }
        else
        {
            logger.debug(" -> No lock found.");

            return false;
        }
    }

    private void initializeStorages(final Map<String, StorageData> storages)
            throws IOException
    {
        logger.info("Running Strongbox storage booter...");
        logger.info(" -> Creating storage directory skeleton...");

        for (Map.Entry<String, StorageData> stringStorageEntry : storages.entrySet())
        {
            initializeStorage(stringStorageEntry.getValue());
        }

    }

    private void initializeStorage(StorageData storage)
            throws IOException
    {
        logger.info("  * Initializing " + storage.getId() + "...");
    }

    private void initializeRepository(RepositoryData repository)
            throws IOException, RepositoryManagementStrategyException
    {
        logger.info("  * Initializing " + repository.getStorage().getId() + ":" + repository.getId() + "...");

        if (layoutProviderRegistry.getProvider(repository.getLayout()) == null)
        {
            logger.error(String.format("Failed to resolve layout [%s] for repository [%s].",
                                       repository.getLayout(),
                                       repository.getId()));
            return;
        }

        repositoryManagementService.createRepository(repository.getStorage().getId(), repository.getId());

        if (RepositoryStatusEnum.IN_SERVICE.getStatus().equals(repository.getStatus()))
        {
            repositoryManagementService.putInService(repository.getStorage().getId(), repository.getId());
        }
    }

    private Collection<RepositoryData> getRepositoriesHierarchy(final Map<String, StorageData> storages)
    {
        final Map<String, RepositoryData> repositoriesHierarchy = new LinkedHashMap<>();
        for (final StorageData storage : storages.values())
        {
            for (final RepositoryData repository : storage.getRepositories().values())
            {
                addRepositoriesByChildrenFirst(repositoriesHierarchy, repository);
            }
        }

        return repositoriesHierarchy.values();
    }

    private void addRepositoriesByChildrenFirst(final Map<String, RepositoryData> repositoriesHierarchy,
                                                final RepositoryData repository)
    {
        if (!repository.isGroupRepository())
        {
            repositoriesHierarchy.putIfAbsent(repository.getId(), repository);

            return;
        }

        groupRepositorySetCollector.collect(repository, true)
                                   .stream().forEach(r -> addRepositoriesByChildrenFirst(repositoriesHierarchy, r));

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

}
