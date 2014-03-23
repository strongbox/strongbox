package org.carlspring.strongbox.storage.repository;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

    public void createRepositoryStructure(String storageBaseDir, String repositoryName)
            throws IOException
    {
        final File storageBasedir = new File(storageBaseDir);
        //noinspection ResultOfMethodCallIgnored
        new File(storageBasedir, repositoryName).mkdirs();

        logger.debug("Created directory structure for repository '" +
                     storageBasedir.getAbsolutePath() + File.separatorChar + repositoryName + "'.");
    }

    public void removeRepositoryStructure(String storageBasedir, String repositoryName)
            throws IOException
    {
        final File repositoryBaseDir = new File(new File(storageBasedir), repositoryName);
        FileUtils.deleteDirectory(repositoryBaseDir);

        logger.debug("Removed directory structure for repository '" +
                     repositoryBaseDir.getAbsolutePath() + File.separatorChar + repositoryName + "'.");
    }

}
