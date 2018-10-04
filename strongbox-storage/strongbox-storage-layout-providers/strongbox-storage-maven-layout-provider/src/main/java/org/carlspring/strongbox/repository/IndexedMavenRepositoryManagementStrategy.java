package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.config.MavenIndexerEnabledCondition;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.cron.config.ApplicationStartupCronTasksInitiator;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.jobs.DownloadRemoteMavenIndexCronJob;
import org.carlspring.strongbox.cron.jobs.RebuildMavenIndexesCronJob;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexerFactory;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
@Conditional(MavenIndexerEnabledCondition.class)
public class IndexedMavenRepositoryManagementStrategy
        extends MavenRepositoryManagementStrategy
{

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    @Inject
    private RepositoryIndexerFactory repositoryIndexerFactory;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private IndexedMavenRepositoryFeatures repositoryFeatures;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Inject
    private ApplicationStartupCronTasksInitiator applicationStartupCronTasksInitiator;

    @Override
    protected void createRepositoryInternal(Storage storage, Repository repository)
            throws IOException, RepositoryManagementStrategyException
    {
        if (repositoryFeatures.isIndexingEnabled(repository))
        {

            String storageId = storage.getId();
            String repositoryId = repository.getId();

            RepositoryPath repositoryBasedir = repositoryPathResolver.resolve(repository);

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

            createRebuildMavenIndexCronJob(storageId, repositoryId);
        }
    }

    private void createRemoteIndexDownloaderCronTask(String storageId,
                                                     String repositoryId)
            throws RepositoryManagementStrategyException
    {
        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
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

    private void createRebuildMavenIndexCronJob(String storageId,
                                                String repositoryId)
            throws RepositoryManagementStrategyException
    {
        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
        configuration.setName("Rebuild Maven Index Cron Job for " + storageId + ":" + repositoryId);
        configuration.addProperty("jobClass", RebuildMavenIndexesCronJob.class.getName());
        configuration.addProperty("cronExpression", "0 0 2 * * ?");
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
                                                     RepositoryPath repositoryBasedir)
            throws IOException
    {
        RepositoryPath repositoryIndexDir = repositoryBasedir.resolve(".index").resolve(indexType);

        if (!Files.exists(repositoryIndexDir))
        {
            //noinspection ResultOfMethodCallIgnored
            Files.createDirectories(repositoryIndexDir);
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
    public void createRepositoryStructure(final Repository repository)
            throws IOException
    {
        super.createRepositoryStructure(repository);
        final RepositoryPath rootRepositoryPath = repositoryPathResolver.resolve(repository);
        final RepositoryPath indexRepositoryPath = rootRepositoryPath.resolve(".index");
        if (!Files.exists(indexRepositoryPath))
        {
            Files.createDirectories(indexRepositoryPath);
        }
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
