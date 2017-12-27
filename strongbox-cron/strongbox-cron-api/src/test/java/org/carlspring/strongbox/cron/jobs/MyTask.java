package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;

/**
 * @author Yougeshwar
 */
public class MyTask
        extends JavaCronJob
{

    @Override
    public void executeTask(CronTaskConfiguration config)
            throws Throwable
    {
        logger.debug("Executed successfully.");
    }

}
