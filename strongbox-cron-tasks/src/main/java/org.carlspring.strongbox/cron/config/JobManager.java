package org.carlspring.strongbox.cron.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

/**
 * @author Kate Novik.
 */
@Component
public class JobManager
        implements IJobManager
{

    private ConcurrentMap<String, Boolean> executedJobs = new ConcurrentHashMap<>();

    public void addExecutedJob(String jobName,
                               Boolean statusExecuted)
    {
        executedJobs.put(jobName, statusExecuted);
    }

    public ConcurrentMap<String, Boolean> getExecutedJobs()
    {
        return executedJobs;
    }

}
