package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.providers.repository.proxied.LocalStorageProxyRepositoryExpiredArtifactsCleaner;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Przemyslaw Fusik
 */
public class CleanupExpiredArtifactsFromProxyRepositoriesCronJob
        extends JavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(CleanupExpiredArtifactsFromProxyRepositoriesCronJob.class);

    @Inject
    private LocalStorageProxyRepositoryExpiredArtifactsCleaner proxyRepositoryObsoleteArtifactsCleaner;

    @Override
    public void executeTask(final CronTaskConfiguration config)
            throws Throwable
    {
        final String lastAccessedTimeInDaysText = config.getRequiredProperty("lastAccessedTimeInDays");
        final String minSizeInBytesText = config.getProperty("minSizeInBytes");

        final Integer lastAccessedTimeInDays;
        try
        {
            lastAccessedTimeInDays = Integer.valueOf(lastAccessedTimeInDaysText);
        }
        catch (NumberFormatException ex)
        {
            logger.error("Invalid integer value [" + lastAccessedTimeInDaysText +
                         "] of 'lastAccessedTimeInDays' property. Cron job won't be fired.", ex);
            return;
        }

        Long minSizeInBytes = Long.valueOf(-1);
        if (minSizeInBytesText != null)
        {
            try
            {
                minSizeInBytes = Long.valueOf(minSizeInBytesText);
            }
            catch (NumberFormatException ex)
            {
                logger.error("Invalid Long value [" + minSizeInBytesText +
                             "] of 'minSizeInBytes' property. Cron job won't be fired.", ex);
                return;
            }
        }

        proxyRepositoryObsoleteArtifactsCleaner.cleanup(lastAccessedTimeInDays, minSizeInBytes);
    }

}
