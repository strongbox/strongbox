package org.carlspring.strongbox.cron.api.jobs;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Kate Novik.
 */
public class RebuildMavenIndexesCronJob
        extends JavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(RebuildMavenIndexesCronJob.class);

    @Autowired
    private ConfigurationManager configurationManager;

    @Autowired
    private ArtifactIndexesService artifactIndexesService;

    @Autowired
    private JobManager manager;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        logger.debug("Executed RebuildMavenIndexesCronJob.");

        CronTaskConfiguration config = (CronTaskConfiguration) jobExecutionContext.getMergedJobDataMap().get("config");
        try
        {
            String storageId = config.getProperty("storageId");
            String repositoryId = config.getProperty("repositoryId");
            String basePath = config.getProperty("basePath");

            if (storageId == null)
            {
                artifactIndexesService.rebuildIndexes();
            }
            else if (repositoryId == null)
            {
                artifactIndexesService.rebuildIndexes(storageId);
            }
            else
            {
                artifactIndexesService.rebuildIndexes(storageId, repositoryId, basePath);
            }
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
        }

        manager.addExecutedJob(config.getName(), true);
    }

//    /**
//     * To rebuild indexes in repositories
//     *
//     * @param storageId String
//     * @throws IOException
//     */
//    private void rebuildRepositoriesIndexes(String storageId)
//            throws IOException
//    {
//        Map<String, Repository> repositories = getRepositories(storageId);
//
//        for (String repository : repositories.keySet())
//        {
//            artifactIndexesService.rebuildIndexes(storageId, repository, null);
//        }
//    }
//
//    private Map<String, Storage> getStorages()
//    {
//        return configurationManager.getConfiguration().getStorages();
//    }
//
//    private Map<String, Repository> getRepositories(String storageId)
//    {
//        return getStorages().get(storageId).getRepositories();
//    }
}
