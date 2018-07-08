package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;

/**
 * @author Yougeshwar
 */
public class MyTask
        extends JavaCronJob
{

    @Override
    public void executeTask(CronTaskConfigurationDto config)
            throws Throwable
    {
        logger.debug("Executed successfully.");
    }

}
