package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.jobs.DownloadRemoteFeedCronJob;
import org.carlspring.strongbox.cron.services.CronTaskDataService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.yaml.configuration.repository.NugetRepositoryConfiguration;

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
    private CronTaskDataService cronTaskDataService;

    @Override
    protected void createRepositoryInternal(Storage storage,
                                            Repository repository)
        throws RepositoryManagementStrategyException
    {
        String storageId = storage.getId();
        String repositoryId = repository.getId();
        NugetRepositoryConfiguration repositoryConfiguration = (NugetRepositoryConfiguration) repository.getRepositoryConfiguration();

        if (repository.isProxyRepository())
        {
            createRemoteFeedDownloaderCronTask(storageId,
                                               repositoryId,
                                               repositoryConfiguration.getCronExpression(),
                                               repositoryConfiguration.isCronEnabled());
        }
    }

    private void createRemoteFeedDownloaderCronTask(String storageId,
                                                    String repositoryId,
                                                    String cronExpression,
                                                    boolean cronEnabled)
        throws RepositoryManagementStrategyException
    {
        String downloadRemoteFeedCronJobName = "Remote feed download for " + storageId + ":" + repositoryId;

        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
        configuration.setName(downloadRemoteFeedCronJobName);
        configuration.setJobClass(DownloadRemoteFeedCronJob.class.getName());
        configuration.setCronExpression(cronExpression);
        configuration.addProperty("storageId", storageId);
        configuration.addProperty("repositoryId", repositoryId);
        configuration.setImmediateExecution(true);
        configuration.setCronEnabled(cronEnabled);

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
