package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.jobs.DownloadRemoteMavenIndexCronJob;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.repository.group.index.MavenIndexGroupRepositoryComponent;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexerFactory;
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
public class MavenRepositoryManagementStrategy
        extends AbstractRepositoryManagementStrategy
{

    private static final Logger logger = LoggerFactory.getLogger(MavenRepositoryManagementStrategy.class);

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    @Inject
    private RepositoryIndexerFactory repositoryIndexerFactory;

    @Inject
    private ArtifactIndexesService artifactIndexesService;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private CronJobSchedulerService cronJobSchedulerService;

    @Inject
    private MavenRepositoryFeatures repositoryFeatures;

    @Inject
    private MavenIndexGroupRepositoryComponent mavenIndexGroupRepositoryComponent;
    
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

            if (repository.isGroupRepository())
            {
                mavenIndexGroupRepositoryComponent.initialize(repository);
            }
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
        catch (Exception e)
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
