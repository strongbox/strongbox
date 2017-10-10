package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
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
    public void executeTask(CronTaskConfiguration config)
            throws Throwable
    {
        logger.debug("Executed MyTask.");

        try
        {
            getSchedulerFactoryBean().getScheduler().deleteJob(getCronTask().getJobDetail().getKey());
        }
        catch (SchedulerException e)
        {
            logger.error("Stop job error", e);

            e.printStackTrace();
        }
    }

}
