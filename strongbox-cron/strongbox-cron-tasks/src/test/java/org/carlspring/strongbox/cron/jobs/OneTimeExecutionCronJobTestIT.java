package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.JobManager;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
@CronTaskTest
@RunWith(SpringJUnit4ClassRunner.class)
public class OneTimeExecutionCronJobTestIT
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

    @After
    public void tearDown()
            throws Exception
    {
        // Un-register to receive cron task-related events
        cronTaskEventListenerRegistry.removeListener(this);
    }

    public void addOneTimeExecutionCronJobConfig(String name)
            throws Exception
    {
        CronTaskConfiguration configuration = new CronTaskConfiguration();
        configuration.setName(name);
        configuration.addProperty("cronExpression", "0 11 11 11 11 ? 2100");
        configuration.addProperty("jobClass", OneTimeExecutionCronJob.class.getName());
        configuration.setOneTimeExecution(true);
        configuration.setImmediateExecution(true);

        addCronJobConfig(configuration);
    }

    @Test
    public void testOneTimeExecutionCronJob()
            throws Exception
    {
        addOneTimeExecutionCronJobConfig(expectedCronTaskName);

        assertTrue("Failed to execute task within a reasonable time!", expectEvent(60000, 500));
    }

}
