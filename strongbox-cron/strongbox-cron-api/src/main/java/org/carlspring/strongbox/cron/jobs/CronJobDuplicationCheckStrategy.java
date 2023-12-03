package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;

import java.util.Collection;

/**
 * @author Przemyslaw Fusik
 */
public interface CronJobDuplicationCheckStrategy
{

    /**
     * Checks whether the `candidate` should be considered as a duplicate
     * of one of the elements from provided `existing` collection
     */
    boolean duplicates(CronTaskConfigurationDto candidate,
                       Collection<CronTaskConfigurationDto> existing);
}
