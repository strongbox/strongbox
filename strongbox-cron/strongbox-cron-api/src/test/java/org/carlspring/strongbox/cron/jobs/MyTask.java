package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;

import java.util.Collections;

/**
 * @author Yougeshwar
 */
public class MyTask
        extends JavaCronJob
{

    @Override
    public void executeTask(CronTaskConfigurationDto config)
    {
        logger.debug("Executed successfully.");
    }

    @Override
    public CronJobDefinition getCronJobDefinition()
    {
        return CronJobDefinition.newBuilder()
                                .jobClass(MyTask.class.getName())
                                .name("My Task")
                                .description("My Task")
                                .fields(Collections.emptySet())
                                .build();
    }
}
