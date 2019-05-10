package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;

import java.util.Collections;

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
    {
        System.out.println("The one-time task has run " + runs + " times.");

        assertFalse(runs > 1, "Failed to execute in single run mode.");

        runs++;
    }

    @Override
    public CronJobDefinition getCronJobDefinition()
    {
        return CronJobDefinition.newBuilder()
                                .jobClass(OneTimeExecutionCronJob.class.getName())
                                .name("One Time Execution Cron Job")
                                .description("One Time Execution Cron Job")
                                .fields(Collections.emptySet())
                                .build();
    }

}

