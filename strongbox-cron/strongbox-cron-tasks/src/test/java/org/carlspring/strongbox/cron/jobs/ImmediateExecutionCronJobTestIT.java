package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.JobManager;
import org.carlspring.strongbox.event.cron.CronTaskEventTypeEnum;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author carlspring
 */
@CronTaskTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ImmediateExecutionCronJobTestIT
        extends BaseCronTestCase
{

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private JobManager jobManager;


    @Before
    public void initialize()
            throws Exception
    {
        // Register to receive cron task-related events
        cronTaskEventListenerRegistry.addListener(this);
    }

    public void addImmediateExecutionCronJobConfig(String name)
            throws Exception
    {
        CronTaskConfiguration configuration = new CronTaskConfiguration();
        configuration.setName(name);
        configuration.addProperty("jobClass", ImmediateExecutionCronJob.class.getName());
        configuration.addProperty("cronExpression", "0 0 * * * ? *");
        configuration.setImmediateExecution(true);

        cronTaskConfigurationService.saveConfiguration(configuration);
        CronTaskConfiguration obj = cronTaskConfigurationService.findOne(name);

        assertNotNull(obj);
    }

    @Test
    public void testImmediateExecutionCronJob()
            throws Exception
    {
        String jobName = "ImmediateExecution";

        // Checking if job was executed
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            assertTrue(jobName1.equals(jobName) && statusExecuted);

            assertNull("Immediate execution cron job failed to start within a reasonable time!",
                       cronTaskConfigurationService.getConfiguration(jobName));
        });

        addImmediateExecutionCronJobConfig(jobName);

        assertNotNull("Failed to create single run cron job!",
                      cronTaskConfigurationService.getConfiguration(jobName));

        assertTrue("Failed to execute task within a reasonable time!",
                   expectEvent(CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType()));
    }

}
