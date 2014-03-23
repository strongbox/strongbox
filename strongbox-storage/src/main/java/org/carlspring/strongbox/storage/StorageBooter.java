package org.carlspring.strongbox.storage;

import org.carlspring.strongbox.storage.repository.RepositoryManager;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

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


    public StorageBooter()
    {
    }

    @PostConstruct
    public void initialize()
            throws IOException
    {
        logger.debug("Running Storagebox booter...");
        logger.debug(" -> Creating storage directory skeleton...");

        File storagesBaseDir = new File("target/storages/storage0");

        //noinspection ResultOfMethodCallIgnored
        storagesBaseDir.mkdirs();

        initializeRepository(storagesBaseDir, "releases");
        initializeRepository(storagesBaseDir, "releases-in-memory");
        initializeRepository(storagesBaseDir, "snapshots");
        initializeRepository(storagesBaseDir, "snapshots-in-memory");
    }

    private void initializeRepository(File storagesBaseDir, String repositoryName)
            throws IOException
    {
        repositoryManager.createRepositoryStructure(storagesBaseDir.getAbsolutePath(), repositoryName);
        logger.debug("    -> Initialized '" + storagesBaseDir.getAbsolutePath() + File.separatorChar + repositoryName + "'.");
    }

    public RepositoryManager getRepositoryManager()
    {
        return repositoryManager;
    }

    public void setRepositoryManager(RepositoryManager repositoryManager)
    {
        this.repositoryManager = repositoryManager;
    }

}
