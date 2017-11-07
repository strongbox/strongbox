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
        final String minSizeText = config.getProperty("minSize");

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

        Integer minSize = null;
        if (minSizeText != null)
        {
            try
            {
                minSize = Integer.valueOf(minSizeText);
            }
            catch (NumberFormatException ex)
            {
                logger.error("Invalid integer value [" + minSizeText +
                             "] of 'minSize' property. Cron job won't be fired.", ex);
                return;
            }
        }

        proxyRepositoryObsoleteArtifactsCleaner.cleanup(uselessnessDays, minSize);
    }

}