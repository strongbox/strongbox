package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.jobs.DownloadRemoteFeedCronJob;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class NugetRepositoryManagementStrategy
        extends AbstractRepositoryManagementStrategy
{

    private static final Logger logger = LoggerFactory.getLogger(NugetRepositoryManagementStrategy.class);

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Override
    protected void createRepositoryInternal(Storage storage,
                                            Repository repository)
        throws RepositoryManagementStrategyException
    {
        String storageId = storage.getId();
        String repositoryId = repository.getId();
        
        if (repository.isProxyRepository())
        {
            createRemoteFeedDownloaderCronTask(storageId, repositoryId);
        }
    }

    private void createRemoteFeedDownloaderCronTask(String storageId,
                                                    String repositoryId)
        throws RepositoryManagementStrategyException
    {
        boolean shouldDownloadRemoteRepositoryFeed = shouldDownloadRemoteRepositoryFeed();

        logger.info(String.format("%s/%s: shouldDownloadRemoteRepositoryFeed-[%s]",
                                  storageId,
                                  repositoryId,
                                  shouldDownloadRemoteRepositoryFeed));

        if (!shouldDownloadRemoteRepositoryFeed)
        {
            return;
        }
    	
        CronTaskConfiguration configuration = new CronTaskConfiguration();
        configuration.setName("Remote feed download for " + storageId + ":" + repositoryId);
        configuration.addProperty("jobClass", DownloadRemoteFeedCronJob.class.getName());
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
            logger.error(e.getMessage(), e);

            throw new RepositoryManagementStrategyException(e.getMessage(), e);
        }
    }
    
    public static boolean shouldDownloadRemoteRepositoryFeed()
    {
        return System.getProperty("strongbox.nuget.download.feed") == null ||
               Boolean.parseBoolean(System.getProperty("strongbox.nuget.download.feed"));
    }

}
