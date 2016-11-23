package org.carlspring.strongbox.cron.config;

import java.util.Map;

/**
 * @author Kate Novik.
 */
public interface JobManager
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
     * @return executed jobs type Map<String, Boolean>
     */
    Map<String, Boolean> getExecutedJobs();

}
