package org.carlspring.strongbox.repository;

import static org.carlspring.strongbox.util.IndexContextHelper.getContextId;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.exceptions.CronTaskException;
import org.carlspring.strongbox.cron.jobs.DownloadRemoteMavenIndexCronJob;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexerFactory;
import org.carlspring.strongbox.storage.repository.Repository;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class MavenRepositoryManagementStrategy
        extends AbstractRepositoryManagementStrategy
{

    private static final Logger logger = LoggerFactory.getLogger(MavenRepositoryManagementStrategy.class);

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    @Inject
    private RepositoryIndexerFactory repositoryIndexerFactory;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private CronJobSchedulerService cronJobSchedulerService;

    @Inject
    private MavenRepositoryFeatures repositoryFeatures;
    
    @Override
    protected void createRepositoryInternal(Storage storage, Repository repository)
            throws IOException, RepositoryManagementStrategyException
    {
        String storageId = storage.getId();
        String repositoryId = repository.getId();

        File repositoryBasedir = getRepositoryBaseDir(storageId, repositoryId);
        
        if (repositoryFeatures.isIndexingEnabled(repository))
        {
            if (repository.isProxyRepository())
            {
                // Create a remote index
                createRepositoryIndexer(storageId, repositoryId, IndexTypeEnum.REMOTE.getType(), repositoryBasedir);

                boolean shouldDownloadIndexes = shouldDownloadAllRemoteRepositoryIndexes();
                boolean shouldDownloadRepositoryIndex = shouldDownloadRepositoryIndex(storageId, repositoryId);

                if (shouldDownloadIndexes || shouldDownloadRepositoryIndex)
                {
                    // TODO: Add a check whether there is such a cron task already created for this repository
                    // TODO: (no need to keep adding new cron tasks upon every boot)

                    // Create a scheduled task for downloading the remote's index
                    createRemoteIndexDownloaderCronTask(storageId, repositoryId);
                }
            }

            // Create a local index
            createRepositoryIndexer(storageId, repositoryId, IndexTypeEnum.LOCAL.getType(), repositoryBasedir);
        }
    }

    private void createRemoteIndexDownloaderCronTask(String storageId,
                                                     String repositoryId)
            throws RepositoryManagementStrategyException
    {
        CronTaskConfiguration configuration = new CronTaskConfiguration();
        configuration.setName("Remote index download for " + storageId + ":" + repositoryId);
        configuration.addProperty("jobClass", DownloadRemoteMavenIndexCronJob.class.getName());
        configuration.addProperty("cronExpression", "0 0 0 * * ?"); // Execute once daily at 00:00:00
        configuration.addProperty("storageId", storageId);
        configuration.addProperty("repositoryId", repositoryId);
        configuration.setImmediateExecution(true);

        try
        {
            cronTaskConfigurationService.saveConfiguration(configuration);
        }
        catch (ClassNotFoundException |
               SchedulerException |
               CronTaskException |
               InstantiationException |
               IllegalAccessException e)
        {
            throw new RepositoryManagementStrategyException(e.getMessage(), e);
        }
    }

    public RepositoryIndexer createRepositoryIndexer(String storageId,
                                                     String repositoryId,
                                                     String indexType,
                                                     File repositoryBasedir)
            throws RepositoryInitializationException
    {
        File repositoryIndexDir = new File(repositoryBasedir, ".index/" + indexType);
        if (!repositoryIndexDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            repositoryIndexDir.mkdirs();
        }

        RepositoryIndexer repositoryIndexer = repositoryIndexerFactory.createRepositoryIndexer(storageId,
                                                                                               repositoryId,
                                                                                               indexType,
                                                                                               repositoryBasedir,
                                                                                               repositoryIndexDir);

        String contextId = storageId + ":" + repositoryId + ":" + indexType;

        repositoryIndexManager.addRepositoryIndexer(contextId, repositoryIndexer);

        return repositoryIndexer;
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
            new File(repositoryDir, ".index").mkdirs();
            //noinspection ResultOfMethodCallIgnored
            new File(repositoryDir, ".trash").mkdirs();
        }
    }

    @Override
    public void initializeRepository(String storageId,
                                     String repositoryId)
            throws RepositoryInitializationException
    {
        // initializeRepositoryIndexes(storageId, repositoryId);
    }

    public void initializeRepositoryIndexes(String storageId,
                                            String repositoryId)
            throws RepositoryInitializationException
    {
        try
        {
            Storage storage = getConfiguration().getStorage(storageId);

            final File repositoryBasedir = new File(storage.getBasedir(), repositoryId);

            Repository repository = storage.getRepository(repositoryId);
            if (repositoryFeatures.isIndexingEnabled(repository))
            {
                initializeRepositoryIndex(storage, repositoryId, IndexTypeEnum.LOCAL.getType(), repositoryBasedir);

                if (repository.isProxyRepository())
                {
                    initializeRepositoryIndex(storage, repositoryId, IndexTypeEnum.REMOTE.getType(), repositoryBasedir);
                }
            }
        }
        catch (RepositoryInitializationException e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    public void initializeRepositoryIndex(Storage storage,
                                          String repositoryId,
                                          String indexType,
                                          File repositoryBasedir)
            throws RepositoryInitializationException
    {
        final File indexDir = new File(repositoryBasedir, ".index/" + indexType);

        RepositoryIndexer repositoryIndexer = repositoryIndexerFactory.createRepositoryIndexer(storage.getId(),
                                                                                               repositoryId,
                                                                                               indexType,
                                                                                               repositoryBasedir,
                                                                                               indexDir);

        String contextId = getContextId(storage.getId(), repositoryId, IndexTypeEnum.LOCAL.getType());

        repositoryIndexManager.addRepositoryIndexer(contextId, repositoryIndexer);
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    public static boolean shouldDownloadAllRemoteRepositoryIndexes()
    {
        return System.getProperty("strongbox.download.indexes") == null ||
               Boolean.parseBoolean(System.getProperty("strongbox.download.indexes"));
    }

    public static boolean shouldDownloadRepositoryIndex(String storageId, String repositoryId)
    {
        return (System.getProperty("strongbox.download.indexes." + storageId + "." + repositoryId) == null ||
               Boolean.parseBoolean(System.getProperty("strongbox.download.indexes." + storageId + "." + repositoryId))) &&
               isIncludedDespiteWildcard(storageId, repositoryId);
    }

    public static boolean isIncludedDespiteWildcard(String storageId, String repositoryId)
    {
        return // is excluded by wildcard
               !Boolean.parseBoolean(System.getProperty("strongbox.download.indexes." + storageId + ".*")) &&
               // and is explicitly included
               Boolean.parseBoolean(System.getProperty("strongbox.download.indexes." + storageId + "." + repositoryId));
    }
    
}
