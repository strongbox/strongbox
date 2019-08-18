package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;

import java.util.Collection;

/**
 * @author Przemyslaw Fusik
 */
public class NoopDuplicationCheckStrategy
        implements CronJobDuplicationCheckStrategy
{

    private static final NoopDuplicationCheckStrategy INSTANCE = new NoopDuplicationCheckStrategy();

    public static NoopDuplicationCheckStrategy getInstance()
    {
        return INSTANCE;
    }


    @Override
    public boolean duplicates(final CronTaskConfigurationDto candidate,
                              final Collection<CronTaskConfigurationDto> existing)
    {
        return false;
    }
}
