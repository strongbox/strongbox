package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kate Novik
 * @author carlspring
 */
public class DownloadRemoteMavenIndexCronJob
        extends OnePerRepositoryJavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(DownloadRemoteMavenIndexCronJob.class);

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
