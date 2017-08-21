package org.carlspring.strongbox.cron.services;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;
import org.carlspring.strongbox.cron.domain.GroovyScriptNames;

import org.quartz.SchedulerException;

/**
 * @author carlspring
 */
public interface CronJobSchedulerService
{

    void scheduleJob(CronTaskConfiguration cronTaskConfiguration)
            throws ClassNotFoundException, SchedulerException;

    void executeJob(CronTaskConfiguration cronTaskConfiguration)
            throws SchedulerException;

    void deleteJob(CronTaskConfiguration cronTaskConfiguration)
                    throws ClassNotFoundException, SchedulerException, CronTaskNotFoundException;

    GroovyScriptNames getGroovyScriptsName();
}
