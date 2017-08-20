package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.cron.services.JobManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.repository.RepositoryInitializationException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
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
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private JobManager manager;


    @Override
    public void executeTask(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        logger.debug("Executing DownloadRemoteIndexCronJob.");

        CronTaskConfiguration config = (CronTaskConfiguration) jobExecutionContext.getMergedJobDataMap().get("config");

        try
        {
            String storageId = config.getProperty("storageId");
            String repositoryId = config.getProperty("repositoryId");

            Storage storage = configurationManagementService.getConfiguration().getStorage(storageId);
            Repository repository = storage.getRepository(repositoryId);

            LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
            MavenRepositoryFeatures features = (MavenRepositoryFeatures) layoutProvider.getRepositoryFeatures();

            features.downloadRemoteIndex(storageId, repositoryId);
        }
        catch (ArtifactTransportException | RepositoryInitializationException e)
        {
            logger.error(e.getMessage(), e);

            manager.addExecutedJob(config.getName(), true);
        }

        logger.debug("Executed DownloadRemoteIndexCronJob.");

        manager.addExecutedJob(config.getName(), true);
    }

}
