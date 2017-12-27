package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;

import javax.inject.Inject;

/**
 * @author Kate Novik
 * @author carlspring
 */
public class DownloadRemoteMavenIndexCronJob
        extends OnePerRepositoryJavaCronJob
{

    @Inject
    private MavenRepositoryFeatures features;

    @Override
    public void executeTask(CronTaskConfiguration config)
            throws Throwable
    {
        logger.debug("Executing DownloadRemoteIndexCronJob.");

        String storageId = config.getProperty("storageId");
        String repositoryId = config.getProperty("repositoryId");

        features.downloadRemoteIndex(storageId, repositoryId);
    }
}
