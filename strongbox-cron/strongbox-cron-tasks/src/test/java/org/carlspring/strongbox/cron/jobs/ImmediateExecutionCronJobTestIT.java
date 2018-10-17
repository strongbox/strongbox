package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.services.JobManager;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
@CronTaskTest
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(profiles = "test")
public class ImmediateExecutionCronJobTestIT
        extends BaseCronTestCase
{

    @Rule
    public TestRule watcher = new TestWatcher()
    {
        @Override
        protected void starting(final Description description)
        {
            expectedCronTaskName = description.getMethodName();
        }
    };

    @Inject
    private JobManager jobManager;

    public void addImmediateExecutionCronJobConfig(String name)
            throws Exception
    {
        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
        configuration.setName(name);
        configuration.addProperty("jobClass", ImmediateExecutionCronJob.class.getName());
        configuration.addProperty("cronExpression", "0 11 11 11 11 ? 2100");
        configuration.setImmediateExecution(true);
        configuration.setOneTimeExecution(true);

        addCronJobConfig(configuration);
    }

    @Test
    public void testImmediateExecutionCronJob()
            throws Exception
    {
        String jobName = expectedCronTaskName;

        // Checking if job was executed
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            assertTrue(jobName1.equals(jobName) && statusExecuted);
        });

        addImmediateExecutionCronJobConfig(jobName);

        assertTrue("Failed to execute task within a reasonable time!", expectEvent());
    }

}
