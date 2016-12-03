package org.carlspring.strongbox.cron.api.jobs;

import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.services.ArtifactManagementService;

import java.io.IOException;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Kate Novik.
 */
public class ClearRepositoryTrashCronJob
        extends JavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(ClearRepositoryTrashCronJob.class);

    @Autowired
    private ArtifactManagementService artifactManagementService;

    @Autowired
    private JobManager manager;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        logger.debug("Executed ClearRepositoryTrashCronJob.");

        CronTaskConfiguration config = (CronTaskConfiguration) jobExecutionContext.getMergedJobDataMap().get("config");

        try
        {
            String storageId = config.getProperty("storageId");
            String repositoryId = config.getProperty("repositoryId");

            if (storageId == null && repositoryId == null)
            {
                artifactManagementService.deleteTrash();
            }
            else
            {
                artifactManagementService.deleteTrash(storageId, repositoryId);
            }
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
        }

        manager.addExecutedJob(config.getName(), true);
    }
}
