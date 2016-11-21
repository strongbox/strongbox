package org.carlspring.strongbox.cron.config;

import java.util.concurrent.ConcurrentMap;

/**
 * @author Kate Novik.
 */
public interface IJobManager
{

    /**
     * Add executed job in map
     *
     * @param jobName        job's name type String
     * @param statusExecuted executed job's status type Boolean
     */
    void addExecutedJob(String jobName,
                        Boolean statusExecuted);

    /**
     * Get map of executed jobs
     *
     * @return executed jobs type ConcurrentMap<String, Boolean>
     */
    ConcurrentMap<String, Boolean> getExecutedJobs();

}
