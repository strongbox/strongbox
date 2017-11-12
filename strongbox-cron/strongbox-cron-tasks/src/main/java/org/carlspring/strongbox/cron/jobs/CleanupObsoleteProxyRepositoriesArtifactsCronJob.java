package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.providers.repository.proxied.LocalStorageProxyRepositoryObsoleteArtifactsCleaner;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Przemyslaw Fusik
 */
public class CleanupObsoleteProxyRepositoriesArtifactsCronJob
        extends JavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(CleanupObsoleteProxyRepositoriesArtifactsCronJob.class);

    @Inject
    private LocalStorageProxyRepositoryObsoleteArtifactsCleaner proxyRepositoryObsoleteArtifactsCleaner;

    @Override
    public void executeTask(final CronTaskConfiguration config)
            throws Throwable
    {
        final String uselessnessDaysText = config.getRequiredProperty("uselessnessDays");
        final String minSizeInBytesText = config.getProperty("minSizeInBytes");

        final Integer uselessnessDays;
        try
        {
            uselessnessDays = Integer.valueOf(uselessnessDaysText);
        }
        catch (NumberFormatException ex)
        {
            logger.error("Invalid integer value [" + uselessnessDaysText +
                         "] of 'uselessnessDays' property. Cron job won't be fired.", ex);
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

        proxyRepositoryObsoleteArtifactsCleaner.cleanup(uselessnessDays, minSizeInBytes);
    }

}