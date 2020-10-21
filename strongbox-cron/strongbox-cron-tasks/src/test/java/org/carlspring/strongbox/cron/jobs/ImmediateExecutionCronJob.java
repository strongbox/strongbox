package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;

/**
 * @author carlspring
 */
public class ImmediateExecutionCronJob
        extends JavaCronJob
{


    @Override
    public void executeTask(CronTaskConfigurationDto config)
    {
        System.out.println("ImmediateExecutionCronJob executed!");
    }


    @Override
    public CronJobDefinition getCronJobDefinition()
    {
        return CronJobDefinition.newBuilder()
                                .jobClass(ImmediateExecutionCronJob.class.getName())
                                .name("Immediate Execution Cron Job")
                                .description("Immediate Execution Cron Job")
                                .fields(java.util.Collections.emptySet())
                                .build();
    }

}

