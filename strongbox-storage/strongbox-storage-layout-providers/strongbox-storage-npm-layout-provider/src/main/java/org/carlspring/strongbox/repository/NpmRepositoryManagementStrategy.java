package org.carlspring.strongbox.repository;

import java.io.File;
import java.io.IOException;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NpmRepositoryManagementStrategy extends AbstractRepositoryManagementStrategy
{

    private static final Logger logger = LoggerFactory.getLogger(NpmRepositoryManagementStrategy.class);

    @Override
    protected void createRepositoryInternal(Storage storage,
                                            Repository repository)
        throws IOException,
        RepositoryManagementStrategyException
    {

    }

    @Override
    public void createRepositoryStructure(String storageBasedirPath,
                                          String repositoryId)
        throws IOException
    {
        logger.info(String.format("Create repository structure for [%s]/[%s]", storageBasedirPath, repositoryId));
        final File storageBasedir = new File(storageBasedirPath);
        final File repositoryDir = new File(storageBasedir, repositoryId);

        if (!repositoryDir.exists())
        {
            repositoryDir.mkdirs();
            new File(repositoryDir, ".trash").mkdirs();
        }
    }

    @Override
    public void initializeRepository(String storageId,
                                     String repositoryId)
        throws RepositoryInitializationException
    {

    }

}
