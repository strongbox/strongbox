package org.carlspring.strongbox.repository;

import javax.inject.Inject;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.jobs.FetchRemoteChangesFeedCronJob;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NpmRepositoryManagementStrategy
        extends AbstractRepositoryManagementStrategy
{

    private static final Logger logger = LoggerFactory.getLogger(NpmRepositoryManagementStrategy.class);

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
            createRemoteChangesFeedFetcherCronTask(storageId, repositoryId);
        }
    }

    private void createRemoteChangesFeedFetcherCronTask(String storageId,
                                                        String repositoryId)
        throws RepositoryManagementStrategyException
    {
        boolean shouldFetchRemoteChangesFeed = shouldDownloadRemoteChangesFeed();

        logger.info(String.format("%s/%s: shouldDownloadRemoteChangesFeed-[%s]",
                                  storageId,
                                  repositoryId,
                                  shouldFetchRemoteChangesFeed));

        if (!shouldFetchRemoteChangesFeed)
        {
            return;
        }

        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
        configuration.setName("Fetch Remote Changes feed for " + storageId + ":" + repositoryId);
        configuration.addProperty("jobClass", FetchRemoteChangesFeedCronJob.class.getName());
        configuration.addProperty("cronExpression", "0 0 * ? * * *"); // Execute every hour
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

    public static boolean shouldDownloadRemoteChangesFeed()
    {
        return System.getProperty("strongbox.npm.remote.changes.enabled") == null ||
                Boolean.parseBoolean(System.getProperty("strongbox.npm.remote.changes.enabled"));
    }

}
