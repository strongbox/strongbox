package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.repository.NugetRepositoryFeatures;

import javax.inject.Inject;

import org.springframework.core.env.Environment;

/**
 * @author Sergey Bespalov
 *
 */
public class DownloadRemoteFeedCronJob
        extends JavaCronJob
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

    @Override
    public boolean enabled(CronTaskConfigurationDto configuration, Environment env)
    {
        if (!super.enabled(configuration, env))
        {
            return false;
        }
        
        return shouldDownloadRemoteRepositoryFeed();
    }

    public static boolean shouldDownloadRemoteRepositoryFeed()
    {
        return System.getProperty("strongbox.nuget.download.feed") == null ||
                Boolean.parseBoolean(System.getProperty("strongbox.nuget.download.feed"));
    }
}
