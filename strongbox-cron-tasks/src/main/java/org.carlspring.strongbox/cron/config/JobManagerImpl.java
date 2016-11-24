package org.carlspring.strongbox.cron.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * @author Kate Novik.
 */
@Component
public class JobManagerImpl
        implements JobManager
{

    private Map<String, Boolean> executedJobs = new ConcurrentHashMap<>();

    public void addExecutedJob(String jobName,
                               Boolean statusExecuted)
    {
        executedJobs.put(jobName, statusExecuted);
    }

    public Map<String, Boolean> getExecutedJobs()
    {
        return executedJobs;
    }

}
