package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.CronTaskDataService;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kate Novik
 * @author carlspring
 */
public class DownloadRemoteMavenIndexCronJob
        extends JavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(DownloadRemoteMavenIndexCronJob.class);

    @Inject
    private MavenRepositoryFeatures features;

    @Inject
    private CronTaskDataService cronTaskDataService;

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;


    @Override
    public void executeTask(CronTaskConfiguration config)
            throws Throwable
    {
        logger.debug("Executing DownloadRemoteIndexCronJob.");

        String storageId = config.getProperty("storageId");
        String repositoryId = config.getProperty("repositoryId");

        features.downloadRemoteIndex(storageId, repositoryId);
    }

    @Override
    public void beforeScheduleCallback(CronTaskConfiguration config)
            throws Exception
    {
        String storageId = config.getProperty("storageId");
        String repositoryId = config.getProperty("repositoryId");

        List<CronTaskConfiguration> previousConfigurations = cronTaskDataService.findBy(storageId, repositoryId, getClass());
        for (CronTaskConfiguration configuration : previousConfigurations)
        {
            // remove previous configurations for the same job and repository
            cronTaskConfigurationService.deleteConfiguration(configuration);
        }
    }
}
