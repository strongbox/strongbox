package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;

import javax.inject.Inject;

/**
 * @author Kate Novik
 * @author carlspring
 */
public class DownloadRemoteMavenIndexCronJob
        extends OnePerRepositoryJavaCronJob
{

    @Inject
    private IndexedMavenRepositoryFeatures features;

    @Override
    public void executeTask(CronTaskConfigurationDto config)
            throws Throwable
    {
        logger.debug("Executing DownloadRemoteIndexCronJob.");

        String storageId = config.getProperty("storageId");
        String repositoryId = config.getProperty("repositoryId");

        features.downloadRemoteIndex(storageId, repositoryId);
    }
}
