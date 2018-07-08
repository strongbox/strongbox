package org.carlspring.strongbox.cron.jobs;

import static org.junit.Assert.assertFalse;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;

/**
 * @author carlspring
 */
public class OneTimeExecutionCronJob
        extends JavaCronJob
{

    int runs = 1;

    @Override
    public void executeTask(CronTaskConfigurationDto config)
            throws Throwable
    {
        System.out.println("The one-time task has run " + runs + " times.");

        assertFalse("Failed to execute in single run mode.", runs > 1);

        runs++;
    }

    public int getRuns()
    {
        return runs;
    }

}

