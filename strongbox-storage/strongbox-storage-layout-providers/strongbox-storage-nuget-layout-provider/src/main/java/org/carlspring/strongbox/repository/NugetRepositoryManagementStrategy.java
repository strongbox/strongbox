package org.carlspring.strongbox.repository;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.exceptions.CronTaskException;
import org.carlspring.strongbox.cron.jobs.DownloadRemoteFeedCronJob;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author carlspring
 */
public class NugetRepositoryManagementStrategy
        extends AbstractRepositoryManagementStrategy
{

    private static final Logger logger = LoggerFactory.getLogger(NugetRepositoryManagementStrategy.class);

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private CronJobSchedulerService cronJobSchedulerService;

    @Override
    protected void createRepositoryInternal(Storage storage,
                                            Repository repository)
        throws IOException,
        RepositoryManagementStrategyException
    {
        String storageId = storage.getId();
        String repositoryId = repository.getId();
        
        if (repository.isProxyRepository())
        {
            createRemoteFeedDownloaderCronTask(storageId, repositoryId);
        }
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
            // noinspection ResultOfMethodCallIgnored
            repositoryDir.mkdirs();
            // noinspection ResultOfMethodCallIgnored
            new File(repositoryDir, ".trash").mkdirs();
        }
    }

    @Override
    public void initializeRepository(String storageId,
                                     String repositoryId)
        throws RepositoryInitializationException
    {

    }

    private void createRemoteFeedDownloaderCronTask(String storageId,
                                                    String repositoryId)
        throws RepositoryManagementStrategyException
    {
        boolean shouldDownloadIndexes = shouldDownloadAllRemoteRepositoryIndexes();
        boolean shouldDownloadRepositoryIndex = shouldDownloadRepositoryIndex(storageId, repositoryId);

        if (shouldDownloadIndexes && shouldDownloadRepositoryIndex)
        {
            return;
        }
    	
        CronTaskConfiguration configuration = new CronTaskConfiguration();
        configuration.setName("Remote feed download for " + storageId + ":" + repositoryId);
        configuration.addProperty("jobClass", DownloadRemoteFeedCronJob.class.getName());
        configuration.addProperty("cronExpression", "0 0 0 * * ?"); // Execute once daily at 00:00:00
        configuration.addProperty("storageId", storageId);
        configuration.addProperty("repositoryId", repositoryId);

        try
        {
            cronTaskConfigurationService.saveConfiguration(configuration);

            // Run the scheduled task once, immediately, so that the remote's index would become available
            cronJobSchedulerService.executeJob(configuration);
        }
        catch (ClassNotFoundException | SchedulerException | CronTaskException | InstantiationException
                | IllegalAccessException e)
        {
            logger.error(e.getMessage(), e);

            throw new RepositoryManagementStrategyException(e.getMessage(), e);
        }
    }
}
