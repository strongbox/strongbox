package org.carlspring.strongbox.cron.services;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;

/**
 * @author carlspring
 */
public interface CronJobSchedulerService
{

    void scheduleJob(CronTaskConfigurationDto cronTaskConfiguration);

    void executeJob(CronTaskConfigurationDto cronTaskConfiguration);

    void deleteJob(String cronTaskConfigurationName);

}
