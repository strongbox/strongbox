package org.carlspring.strongbox.booters;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexerFactory;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryManager;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class StorageBooter
{

    private static final Logger logger = LoggerFactory.getLogger(StorageBooter.class);

    @Autowired
    private RepositoryManager repositoryManager;

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;

    @Autowired
    private RepositoryIndexerFactory repositoryIndexerFactory;

    @Autowired
    private ConfigurationManager configurationManager;


    public StorageBooter()
    {
    }

    @PostConstruct
    public void initialize()
            throws IOException,
                   PlexusContainerException,
                   ComponentLookupException
    {
        File storagesBaseDir = initializeStorages();

        if (!lockExists(storagesBaseDir))
        {
            createLockFile(storagesBaseDir);

            final Configuration configuration = configurationManager.getConfiguration();
            for (String storageKey : configuration.getStorages().keySet())
            {
                Storage storage = configuration.getStorages().get(storageKey);
                initializeRepositories(storage, new File(storagesBaseDir, storage.getId()));
            }
        }
        else
        {
            logger.debug("Failed to initialize the repositories. Another JVM may have already done this.");
        }
    }

    private void createLockFile(File storagesRootDir)
            throws IOException
    {
        final File lockFile = new File(storagesRootDir, "storage-booter.lock");
        //noinspection ResultOfMethodCallIgnored
        lockFile.getParentFile().mkdirs();
        //noinspection ResultOfMethodCallIgnored
        lockFile.createNewFile();

        logger.debug(" -> Created lock file '" + lockFile.getAbsolutePath() + "'...");
    }

    private boolean lockExists(File storagesRootDir)
            throws IOException
    {
        File lockFile = new File(storagesRootDir, "storage-booter.lock");

        if (lockFile.exists())
        {
            logger.debug(" -> Lock found: '" + storagesRootDir + "'!");

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
     * @throws IOException
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
            basedir = "target/storages";
        }

        final Map<String,Storage> storages = configurationManager.getConfiguration().getStorages();
        for (String storageId : storages.keySet())
        {
            Storage storage = storages.get(storageId);

            initializeStorage(storage.getBasedir(), storages.get(storageId));
        }

        return new File(basedir).getAbsoluteFile();
    }

    private File initializeStorage(String basedir, Storage storage)
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

    private void initializeRepositories(Storage storage,
                                        File storageBaseDir)
            throws IOException,
                   PlexusContainerException,
                   ComponentLookupException
    {
        for (Repository repository : storage.getRepositories().values())
        {
            initializeRepository(storage.getId(), repository.getId(), storageBaseDir);
            repository.setStorage(storage);
        }
    }

    private void initializeRepository(String storageId,
                                      String repositoryId,
                                      File storagesBaseDir)
            throws IOException,
                   PlexusContainerException,
                   ComponentLookupException
    {
        repositoryManager.createRepositoryStructure(storagesBaseDir.getAbsolutePath(), repositoryId);

        initializeRepositoryIndex(storageId, new File(storagesBaseDir.getAbsoluteFile(), repositoryId), repositoryId);

        logger.debug("    -> Initialized '" + storagesBaseDir.getAbsolutePath() + File.separatorChar + repositoryId + "'.");
    }

    private void initializeRepositoryIndex(String storageId,
                                           File repositoryBasedir,
                                           String repositoryId)
            throws PlexusContainerException,
                   ComponentLookupException,
                   IOException
    {
        final File indexDir = new File(repositoryBasedir, ".index");

        RepositoryIndexer repositoryIndexer = repositoryIndexerFactory.createRepositoryIndexer(storageId,
                                                                                               repositoryId,
                                                                                               repositoryBasedir,
                                                                                               indexDir);

        repositoryIndexManager.addRepositoryIndex(storageId + ":" + repositoryId, repositoryIndexer);
    }

    public RepositoryManager getRepositoryManager()
    {
        return repositoryManager;
    }

    public void setRepositoryManager(RepositoryManager repositoryManager)
    {
        this.repositoryManager = repositoryManager;
    }

    public RepositoryIndexManager getRepositoryIndexManager()
    {
        return repositoryIndexManager;
    }

    public void setRepositoryIndexManager(RepositoryIndexManager repositoryIndexManager)
    {
        this.repositoryIndexManager = repositoryIndexManager;
    }

}
