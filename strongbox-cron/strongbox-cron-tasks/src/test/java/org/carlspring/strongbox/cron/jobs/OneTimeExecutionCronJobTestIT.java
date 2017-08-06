package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author carlspring
 */
@CronTaskTest
@RunWith(SpringJUnit4ClassRunner.class)
public class OneTimeExecutionCronJobTestIT
{

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private JobManager jobManager;


    public void addSingleRunCronJobConfig(String name)
            throws Exception
    {
        CronTaskConfiguration cronTaskConfiguration = new CronTaskConfiguration();
        cronTaskConfiguration.setName(name);
        cronTaskConfiguration.addProperty("jobClass", SingleRunCronJob.class.getName());
        cronTaskConfiguration.addProperty("cronExpression", "0 0/1 * 1/1 * ? *");

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);
        CronTaskConfiguration obj = cronTaskConfigurationService.findOne(name);

        assertNotNull(obj);
    }

    @Test
    public void testSingleRunCronJob()
            throws Exception
    {
        String jobName = "SingleRunCronJob-01";

        // Checking if job was executed
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            assertTrue(jobName1.equals(jobName) && statusExecuted);

            assertNull("Single run cron job failed to remove itself after executing!",
                       cronTaskConfigurationService.getConfiguration(jobName));
        });

        addSingleRunCronJobConfig(jobName);

        assertNotNull("Failed to create single run cron job!",
                      cronTaskConfigurationService.getConfiguration(jobName));

        // Leaving this for manual double-checking, in case it's ever needed again
        // Thread.sleep(65000);
    }

}
