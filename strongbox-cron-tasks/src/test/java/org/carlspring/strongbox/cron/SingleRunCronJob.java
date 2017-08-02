package org.carlspring.strongbox.cron;

import org.carlspring.strongbox.cron.api.jobs.JavaCronJob;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import static org.junit.Assert.assertFalse;

/**
 * @author carlspring
 */
public class SingleRunCronJob
        extends JavaCronJob
{

    int runs = 1;

    @Override
    public void executeTask(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        System.out.println("The single run task has run " + runs + " times.");

        assertFalse("Failed to execute in single run mode.", runs > 1);

        runs++;
    }

    public int getRuns()
    {
        return runs;
    }

}

