package org.carlspring.strongbox.common.storage.repository;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * @author mtodorov
 */
@Component
public class RepositoryManager
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryManager.class);


    public RepositoryManager()
    {
    }

    public void createRepositoryStructure(String storageBaseDir, String repositoryId)
            throws IOException
    {
        final File storageBasedir = new File(storageBaseDir);
        //noinspection ResultOfMethodCallIgnored
        new File(storageBasedir, repositoryId).mkdirs();
        //noinspection ResultOfMethodCallIgnored
        new File(storageBasedir, repositoryId + File.separatorChar + ".index").mkdirs();
        //noinspection ResultOfMethodCallIgnored
        new File(storageBasedir, repositoryId + File.separatorChar + ".trash").mkdirs();
    }

    public void removeRepositoryStructure(String storageBasedir, String repositoryId)
            throws IOException
    {
        final File repositoryBaseDir = new File(new File(storageBasedir), repositoryId);
        FileUtils.deleteDirectory(repositoryBaseDir);

        logger.debug("Removed directory structure for repository '" +
                     repositoryBaseDir.getAbsolutePath() + File.separatorChar + repositoryId + "'.");
    }

}
