package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class RawRepositoryManagementStrategy
        extends AbstractRepositoryManagementStrategy
{

    private static final Logger logger = LoggerFactory.getLogger(RawRepositoryManagementStrategy.class);

    @Inject
    private ConfigurationManager configurationManager;


    @Override
    protected void createRepositoryInternal(Storage storage, Repository repository)
            throws IOException,
                   RepositoryManagementStrategyException
    {
    }

    @Override
    public void createRepositoryStructure(String storageBasedirPath,
                                          String repositoryId)
            throws IOException
    {
        final File storageBasedir = new File(storageBasedirPath);
        final File repositoryDir = new File(storageBasedir, repositoryId);

        if (!repositoryDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            repositoryDir.mkdirs();
            //noinspection ResultOfMethodCallIgnored
            new File(repositoryDir, ".trash").mkdirs();
        }
    }

    @Override
    public void initializeRepository(String storageId,
                                     String repositoryId)
            throws RepositoryInitializationException
    {
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
