package org.carlspring.strongbox.cron.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import static org.junit.Assert.assertFalse;

/**
 * @author carlspring
 */
public class ImmediateExecutionCronJob
        extends JavaCronJob
{


    @Override
    public void executeTask(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        System.out.println("ImmediateExecutionCronJob executed!");
    }

}

