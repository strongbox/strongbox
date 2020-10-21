package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.services.JobManager;

import javax.inject.Inject;
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
public class ImmediateExecutionCronJobTestIT
        extends BaseCronTestCase
{

    @Inject
    private JobManager jobManager;

    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);
    }

    public void addImmediateExecutionCronJobConfig(final UUID uuid, final String name)
            throws Exception
    {
        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
        configuration.setUuid(uuid);
        configuration.setName(name);
        configuration.setJobClass(ImmediateExecutionCronJob.class.getName());
        configuration.setCronExpression("0 11 11 11 11 ? 2100");
        configuration.setImmediateExecution(true);
        configuration.setOneTimeExecution(true);

        addCronJobConfig(configuration);
    }

    @Test
    public void testImmediateExecutionCronJob()
            throws Exception
    {
        final UUID jobKey = expectedCronTaskUuid;
        final String jobName = expectedCronTaskName;

        // Checking if job was executed
        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            assertThat(jobKey.toString()).isEqualTo(jobKey1);
            assertThat(statusExecuted).isTrue();
        });

        addImmediateExecutionCronJobConfig(jobKey, jobName);

        assertThat(expectEvent()).as("Failed to execute task within a reasonable time!").isTrue();
    }

}
