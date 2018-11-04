package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;

import static org.junit.jupiter.api.Assertions.assertFalse;

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

        assertFalse(runs > 1, "Failed to execute in single run mode.");

        runs++;
    }

    public int getRuns()
    {
        return runs;
    }

}

