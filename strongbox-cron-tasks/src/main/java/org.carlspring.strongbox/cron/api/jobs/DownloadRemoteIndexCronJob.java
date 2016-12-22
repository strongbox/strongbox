package org.carlspring.strongbox.cron.api.jobs;

import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.services.ArtifactIndexesService;

import java.io.IOException;

import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Kate Novik.
 */
public class DownloadRemoteIndexCronJob
        extends JavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(DownloadRemoteIndexCronJob.class);

    @Autowired
    private ArtifactIndexesService artifactIndexesService;

    @Autowired
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

            artifactIndexesService.downloadRemoteIndex(storageId, repositoryId);

        }
        catch (IOException | ComponentLookupException | PlexusContainerException e)
        {
            logger.error(e.getMessage(), e);
        }

        manager.addExecutedJob(config.getName(), true);
    }
}
