package org.carlspring.strongbox.cron.services;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;
import org.carlspring.strongbox.cron.domain.GroovyScriptNamesDto;

import org.quartz.SchedulerException;

/**
 * @author carlspring
 */
public interface CronJobSchedulerService
{

    void scheduleJob(CronTaskConfigurationDto cronTaskConfiguration)
            throws ClassNotFoundException, SchedulerException;

    void executeJob(CronTaskConfigurationDto cronTaskConfiguration)
            throws SchedulerException;

    void deleteJob(String cronTaskConfigurationName)
                    throws ClassNotFoundException, SchedulerException, CronTaskNotFoundException;

    GroovyScriptNamesDto getGroovyScriptsName();
}
