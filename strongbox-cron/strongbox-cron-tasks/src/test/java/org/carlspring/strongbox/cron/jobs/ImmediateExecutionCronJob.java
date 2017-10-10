package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;

/**
 * @author carlspring
 */
public class ImmediateExecutionCronJob
        extends JavaCronJob
{


    @Override
    public void executeTask(CronTaskConfiguration config)
            throws Throwable
    {
        System.out.println("ImmediateExecutionCronJob executed!");
    }

}

