package org.carlspring.strongbox.cron.api.jobs;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.repository.RepositoryInitializationException;

import javax.inject.Inject;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kate Novik.
 */
public class DownloadRemoteIndexCronJob
        extends JavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(DownloadRemoteIndexCronJob.class);

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    private JobManager manager;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        logger.debug("Executed DownloadRemoteIndexCronJob.");

        CronTaskConfiguration config = (CronTaskConfiguration) jobExecutionContext.getMergedJobDataMap()
                                                                                  .get("config");
        try
        {
            String storageId = config.getProperty("storageId");
            String repositoryId = config.getProperty("repositoryId");

            repositoryManagementService.downloadRemoteIndex(storageId, repositoryId);

        }
        catch (ArtifactTransportException | RepositoryInitializationException e)
        {
            logger.error(e.getMessage(), e);
        }

        manager.addExecutedJob(config.getName(), true);
    }

}
