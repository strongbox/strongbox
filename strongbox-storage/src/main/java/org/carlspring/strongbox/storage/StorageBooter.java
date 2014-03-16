package org.carlspring.strongbox.storage;

import javax.annotation.PostConstruct;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class StorageBooter
{

    private static final Logger logger = LoggerFactory.getLogger(StorageBooter.class);


    public StorageBooter()
    {
    }

    @PostConstruct
    public void initialize()
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
    {
        //noinspection ResultOfMethodCallIgnored
        new File(storagesBaseDir, repositoryName).mkdirs();
        logger.debug("     -> Initialized '" + storagesBaseDir.getAbsolutePath() + File.separatorChar + repositoryName + "'.");
    }

}
