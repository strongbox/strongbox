package org.carlspring.strongbox.cron.config;

public interface JobExecutionListener
{

    void onJobExecution(String jobName,
                        Boolean statusExecuted);
}
