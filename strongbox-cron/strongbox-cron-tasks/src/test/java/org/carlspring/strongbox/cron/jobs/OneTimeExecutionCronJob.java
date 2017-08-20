package org.carlspring.strongbox.cron.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import static org.junit.Assert.assertFalse;

/**
 * @author carlspring
 */
public class OneTimeExecutionCronJob
        extends JavaCronJob
{

    int runs = 1;

    @Override
    public void executeTask(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
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

