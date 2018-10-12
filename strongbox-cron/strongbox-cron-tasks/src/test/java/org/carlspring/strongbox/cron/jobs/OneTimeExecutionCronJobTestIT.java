package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;

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
public class OneTimeExecutionCronJobTestIT
        extends BaseCronTestCase
{

    public void addOneTimeExecutionCronJobConfig(String name)
            throws Exception
    {
        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
        configuration.setName(name);
        configuration.addProperty("cronExpression", "0 11 11 11 11 ? 2100");
        configuration.addProperty("jobClass", OneTimeExecutionCronJob.class.getName());
        configuration.setOneTimeExecution(true);
        configuration.setImmediateExecution(true);

        addCronJobConfig(configuration);
    }

    @Test
    public void testOneTimeExecutionCronJob(TestInfo testInfo)
            throws Exception
    {
        expectedCronTaskName = testInfo.getDisplayName();
        addOneTimeExecutionCronJobConfig(expectedCronTaskName);

        assertTrue(expectEvent(60000, 500), "Failed to execute task within a reasonable time!");
    }

}
