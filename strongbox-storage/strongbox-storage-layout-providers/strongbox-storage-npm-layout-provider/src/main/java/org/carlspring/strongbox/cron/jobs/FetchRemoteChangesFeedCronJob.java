package org.carlspring.strongbox.cron.jobs;

import javax.inject.Inject;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.repository.NpmRepositoryFeatures;

/**
 * @author Sergey Bespalov
 *
 */
public class FetchRemoteChangesFeedCronJob
        extends OnePerRepositoryJavaCronJob
{

    @Inject
    private NpmRepositoryFeatures features;

    @Override
    public void executeTask(CronTaskConfigurationDto config)
        throws Throwable
    {
        String storageId = config.getProperty("storageId");
        String repositoryId = config.getProperty("repositoryId");

        features.fetchRemoteChangesFeed(storageId, repositoryId);
    }

}
