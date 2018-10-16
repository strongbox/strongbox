package org.carlspring.strongbox.cron.jobs;

import javax.inject.Inject;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.repository.NpmRepositoryFeatures;
import org.springframework.core.env.Environment;

/**
 * @author Sergey Bespalov
 *
 */
public class FetchRemoteNpmChangesFeedCronJob
        extends JavaCronJob
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

    public static String calculateJobName(String storageId,
                                          String repositoryId)
    {
        return String.format("Fetch Remote Changes feed for %s:%s", storageId, repositoryId);
    }
    
    @Override
    public boolean enabled(CronTaskConfigurationDto configuration, Environment env)
    {
        if (!super.enabled(configuration, env))
        {
            return false;
        }
        
        return shouldDownloadRemoteChangesFeed();
    }

    public static boolean shouldDownloadRemoteChangesFeed()
    {
        return System.getProperty("strongbox.npm.remote.changes.enabled") == null ||
               Boolean.parseBoolean(System.getProperty("strongbox.npm.remote.changes.enabled"));
    }
}
