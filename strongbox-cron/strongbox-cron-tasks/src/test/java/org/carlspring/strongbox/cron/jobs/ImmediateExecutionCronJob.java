package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;

/**
 * @author carlspring
 */
public class ImmediateExecutionCronJob
        extends JavaCronJob
{


    @Override
    public void executeTask(CronTaskConfigurationDto config)
            throws Throwable
    {
        System.out.println("ImmediateExecutionCronJob executed!");
    }

}

