package org.carlspring.strongbox.cron.api.jobs;

import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.services.RepositoryManagementService;

import javax.inject.Inject;
import javax.inject.Named;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kate Novik.
 */
public class ClearRepositoryTrashCronJob
        extends JavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(ClearRepositoryTrashCronJob.class);

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    private JobManager manager;


    @Override
    public void executeTask(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        final String jobClassName = getClass().getName();

        logger.debug("Execute " + jobClassName);

        CronTaskConfiguration config = (CronTaskConfiguration) jobExecutionContext.getMergedJobDataMap().get("config");

        try
        {
            String storageId = config.getProperty("storageId");
            String repositoryId = config.getProperty("repositoryId");

            if (storageId == null && repositoryId == null)
            {
                repositoryManagementService.deleteTrash();
            }
            else
            {
                repositoryManagementService.deleteTrash(storageId, repositoryId);
            }
        }
        catch (Exception e)
        {
            logger.error("Unable to execute " + jobClassName + ". " + e.getMessage(), e);
        }

        // notify about job execution in any case
        manager.addExecutedJob(config.getName(), true);
    }

}
