package org.carlspring.strongbox.booters;

import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexerFactory;
import org.carlspring.strongbox.storage.repository.RepositoryManager;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

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


    public StorageBooter()
    {
    }

    @PostConstruct
    public void initialize()
            throws IOException,
                   PlexusContainerException,
                   ComponentLookupException
    {
        File storagesBaseDir = initializeStorage();

        if (!lockExists(storagesBaseDir))
        {
            createLockFile(storagesBaseDir);
            initializeRepositories(storagesBaseDir);
        }
        else
        {
            System.out.println("Failed to initialize the repositories. Another JVM may have already done this.");
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

    private File initializeStorage()
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

        final File storagesBaseDir = new File(basedir, "storage0");

        //noinspection ResultOfMethodCallIgnored
        storagesBaseDir.mkdirs();

        return storagesBaseDir;
    }

    private void initializeRepositories(File storagesBaseDir)
            throws IOException,
                   PlexusContainerException,
                   ComponentLookupException
    {
        initializeRepository(storagesBaseDir, "releases");
        initializeRepository(storagesBaseDir, "releases-in-memory");
        initializeRepository(storagesBaseDir, "releases-with-trash");
        initializeRepository(storagesBaseDir, "releases-with-redeployment");
        initializeRepository(storagesBaseDir, "snapshots");
        initializeRepository(storagesBaseDir, "snapshots-in-memory");
    }

    private void initializeRepository(File storagesBaseDir,
                                      String repositoryName)
            throws IOException,
                   PlexusContainerException,
                   ComponentLookupException
    {
        repositoryManager.createRepositoryStructure(storagesBaseDir.getAbsolutePath(), repositoryName);

        initializeRepositoryIndex(new File(storagesBaseDir.getAbsoluteFile(), repositoryName), repositoryName);

        logger.debug("    -> Initialized '" + storagesBaseDir.getAbsolutePath() + File.separatorChar + repositoryName + "'.");
    }

    private void initializeRepositoryIndex(File repositoryBasedir,
                                           String repositoryId)
            throws PlexusContainerException,
                   ComponentLookupException,
                   IOException
    {
        final File indexDir = new File(repositoryBasedir, ".index");

        RepositoryIndexer repositoryIndexer = repositoryIndexerFactory.createRepositoryIndexer(repositoryId,
                                                                                               repositoryBasedir,
                                                                                               indexDir);

        repositoryIndexManager.addRepositoryIndex("storage0:" + repositoryId, repositoryIndexer);
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
