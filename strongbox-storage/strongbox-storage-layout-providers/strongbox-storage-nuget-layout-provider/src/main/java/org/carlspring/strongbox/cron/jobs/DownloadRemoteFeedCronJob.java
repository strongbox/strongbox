package org.carlspring.strongbox.cron.jobs;

import javax.inject.Inject;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.repository.NugetRepositoryFeatures;

/**
 * @author Sergey Bespalov
 *
 */
public class DownloadRemoteFeedCronJob
        extends OnePerRepositoryJavaCronJob
{

    @Inject
    private NugetRepositoryFeatures features;
    
    @Override
    public void executeTask(CronTaskConfigurationDto config)
        throws Throwable
    {
        String storageId = config.getProperty("storageId");
        String repositoryId = config.getProperty("repositoryId");

        features.downloadRemoteFeed(storageId, repositoryId);
    }

}
