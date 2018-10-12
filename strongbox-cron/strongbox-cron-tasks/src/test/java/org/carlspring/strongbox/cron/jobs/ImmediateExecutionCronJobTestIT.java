package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.services.JobManager;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author carlspring
 */
@CronTaskTest
@ExtendWith(SpringExtension.class)
public class ImmediateExecutionCronJobTestIT
        extends BaseCronTestCase
{

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
    public void testImmediateExecutionCronJob(TestInfo testInfo)
            throws Exception
    {
        expectedCronTaskName = testInfo.getDisplayName();
        String jobName = expectedCronTaskName;

        // Checking if job was executed
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            assertTrue(jobName1.equals(jobName) && statusExecuted);
        });

        addImmediateExecutionCronJobConfig(jobName);

        assertTrue(expectEvent(), "Failed to execute task within a reasonable time!");
    }

}
