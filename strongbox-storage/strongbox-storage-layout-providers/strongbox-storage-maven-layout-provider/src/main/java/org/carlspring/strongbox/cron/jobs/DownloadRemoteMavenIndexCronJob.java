package org.carlspring.strongbox.cron.jobs;

import javax.inject.Inject;

import org.carlspring.strongbox.config.MavenIndexerEnabledCondition;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.springframework.core.env.Environment;

/**
 * @author Kate Novik
 * @author carlspring
 */
public class DownloadRemoteMavenIndexCronJob
        extends JavaCronJob
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

    @Override
    public boolean enabled(CronTaskConfigurationDto configuration, Environment env)
    {
        if (!super.enabled(configuration, env))
        {
            return false;
        }
        
        boolean mavenIndexerEnabled = Boolean.parseBoolean(env.getProperty(MavenIndexerEnabledCondition.MAVEN_INDEXER_ENABLED));
        if (!mavenIndexerEnabled)
        {
            return false;
        }
        
        String storageId = configuration.getProperty("storageId");
        String repositoryId = configuration.getProperty("storageId");
        
        boolean shouldDownloadIndexes = shouldDownloadAllRemoteRepositoryIndexes();
        boolean shouldDownloadRepositoryIndex = shouldDownloadRepositoryIndex(storageId, repositoryId);

        return shouldDownloadIndexes || shouldDownloadRepositoryIndex;
    }

    public static boolean shouldDownloadAllRemoteRepositoryIndexes()
    {
        return System.getProperty("strongbox.download.indexes") == null ||
                Boolean.parseBoolean(System.getProperty("strongbox.download.indexes"));
    }

    public static boolean shouldDownloadRepositoryIndex(String storageId,
                                                        String repositoryId)
    {
        return (System.getProperty("strongbox.download.indexes." + storageId + "." + repositoryId) == null ||
                Boolean.parseBoolean(System.getProperty("strongbox.download.indexes." + storageId + "."
                        + repositoryId)))
                &&
                isIncludedDespiteWildcard(storageId, repositoryId);
    }

    public static boolean isIncludedDespiteWildcard(String storageId,
                                                    String repositoryId)
    {
        return // is excluded by wildcard
        !Boolean.parseBoolean(System.getProperty("strongbox.download.indexes." + storageId + ".*")) &&
        // and is explicitly included
                Boolean.parseBoolean(System.getProperty("strongbox.download.indexes." + storageId + "."
                        + repositoryId));
    }

}
