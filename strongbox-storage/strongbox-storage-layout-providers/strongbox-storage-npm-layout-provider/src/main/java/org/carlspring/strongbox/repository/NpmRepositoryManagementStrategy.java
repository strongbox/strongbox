package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.jobs.FetchRemoteNpmChangesFeedCronJob;
import org.carlspring.strongbox.cron.services.CronTaskDataService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NpmRepositoryManagementStrategy
        extends AbstractRepositoryManagementStrategy
{

    private static final Logger logger = LoggerFactory.getLogger(NpmRepositoryManagementStrategy.class);

    @Inject
    private CronTaskDataService cronTaskDataService;

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
        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
        configuration.setName(FetchRemoteNpmChangesFeedCronJob.calculateJobName(storageId, repositoryId));
        configuration.setJobClass(FetchRemoteNpmChangesFeedCronJob.class.getName());
        configuration.setCronExpression("0 0 * ? * * *"); // Execute every hour
        configuration.addProperty("storageId", storageId);
        configuration.addProperty("repositoryId", repositoryId);
        configuration.setImmediateExecution(true);

        try
        {
            cronTaskDataService.save(configuration);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);

            throw new RepositoryManagementStrategyException(e.getMessage(), e);
        }
    }

}
