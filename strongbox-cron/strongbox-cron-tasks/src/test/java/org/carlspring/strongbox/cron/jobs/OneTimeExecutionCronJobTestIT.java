package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author carlspring
 */
@CronTaskTest
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class OneTimeExecutionCronJobTestIT
        extends BaseCronTestCase
{
    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);
    }

    public void addOneTimeExecutionCronJobConfig(final UUID uuid, final String name)
            throws Exception
    {
        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
        configuration.setUuid(uuid);
        configuration.setName(name);
        configuration.setCronExpression("0 11 11 11 11 ? 2100");
        configuration.setJobClass(OneTimeExecutionCronJob.class.getName());
        configuration.setOneTimeExecution(true);
        configuration.setImmediateExecution(true);

        addCronJobConfig(configuration);
    }

    @Test
    public void testOneTimeExecutionCronJob()
            throws Exception
    {
        addOneTimeExecutionCronJobConfig(expectedCronTaskUuid, expectedCronTaskName);

        assertThat(expectEvent(60000, 500))
                .as("Failed to execute task within a reasonable time!")
                .isTrue();
    }

}
