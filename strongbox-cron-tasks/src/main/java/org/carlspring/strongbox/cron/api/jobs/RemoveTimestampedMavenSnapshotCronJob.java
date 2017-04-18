package org.carlspring.strongbox.cron.api.jobs;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kate Novik.
 */
public class RemoveTimestampedMavenSnapshotCronJob
        extends JavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(RemoveTimestampedMavenSnapshotCronJob.class);

    @Inject
    private ArtifactManagementService artifactManagementService;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private JobManager manager;


    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        logger.debug("Executed RemoveTimestampedMavenSnapshotCronJob.");

        CronTaskConfiguration config = (CronTaskConfiguration) jobExecutionContext.getMergedJobDataMap().get("config");
        
        try
        {
            String storageId = config.getProperty("storageId");
            String repositoryId = config.getProperty("repositoryId");
            String basePath = config.getProperty("basePath");

            //the number of artifacts to keep
            int numberToKeep = Integer.valueOf(config.getProperty("numberToKeep"));
            //the period to keep artifacts (the number of days)
            int keepPeriod = Integer.valueOf(config.getProperty("keepPeriod"));

            if (storageId == null)
            {
                Map<String, Storage> storages = getStorages();
                for (String storage : storages.keySet())
                {
                    removeTimestampedSnapshotArtifacts(storage, numberToKeep, keepPeriod);
                }
            }
            else if (repositoryId == null)
            {
                removeTimestampedSnapshotArtifacts(storageId, numberToKeep, keepPeriod);
            }
            else
            {
                artifactManagementService.removeTimestampedSnapshots(storageId,
                                                                     repositoryId,
                                                                     basePath,
                                                                     numberToKeep,
                                                                     keepPeriod);
            }
        }
        catch (IOException | XmlPullParserException | NoSuchAlgorithmException e)
        {
            logger.error(e.getMessage(), e);
        }

        manager.addExecutedJob(config.getName(), true);
    }

    /**
     * To remove timestamped snapshot artifacts in repositories
     *
     * @param storageId    path of storage
     * @param numberToKeep the number of artifacts to keep
     * @param keepPeriod   the period to keep artifacts (the number of days)
     * @throws NoSuchAlgorithmException
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void removeTimestampedSnapshotArtifacts(String storageId,
                                                    int numberToKeep,
                                                    int keepPeriod)
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        Map<String, Repository> repositories = getRepositories(storageId);

        repositories.forEach((repositoryId, repository) ->
                             {
                                 if (repository.getPolicy().equals(RepositoryPolicyEnum.SNAPSHOT.getPolicy()))
                                 {
                                     try
                                     {
                                         artifactManagementService.removeTimestampedSnapshots(storageId,
                                                                                              repositoryId,
                                                                                              null,
                                                                                              numberToKeep,
                                                                                              keepPeriod);
                                     }
                                     catch (IOException e)
                                     {
                                         logger.error(e.getMessage(), e);
                                     }
                                 }
                             });
    }

    private Map<String, Storage> getStorages()
    {
        return configurationManager.getConfiguration().getStorages();
    }

    private Map<String, Repository> getRepositories(String storageId)
    {
        return getStorages().get(storageId).getRepositories();
    }

}
