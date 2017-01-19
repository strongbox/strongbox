package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.cron.api.jobs.JavaCronJob;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yougeshwar
 */
public class MyTask
        extends JavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(MyTask.class);

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        logger.debug("Executed MyTask.");

        try
        {
            getSchedulerFactoryBean().getScheduler()
                                     .deleteJob(getCronTask().getJobDetail()
                                                             .getKey());
        }
        catch (SchedulerException e)
        {
            logger.error("Stop job error", e);
            e.printStackTrace();
        }

    }
}
