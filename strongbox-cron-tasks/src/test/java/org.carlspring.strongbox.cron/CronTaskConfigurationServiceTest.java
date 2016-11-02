package org.carlspring.strongbox.cron;

import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.exceptions.CronTaskException;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@CronTaskTest
@RunWith(SpringJUnit4ClassRunner.class)
public class CronTaskConfigurationServiceTest
{

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Test
    public void testCronTaskConfiguration()
            throws ClassNotFoundException,
                   SchedulerException,
                   CronTaskNotFoundException,
                   CronTaskException,
                   IllegalAccessException,
                   InstantiationException
    {
        addConfig();
        updateConfig();
        deleteConfig();
    }

    public void addConfig()
            throws SchedulerException,
                   ClassNotFoundException,
                   CronTaskException,
                   InstantiationException,
                   IllegalAccessException
    {
        String name = "Cron-Task-1";
        CronTaskConfiguration cronTaskConfiguration = new CronTaskConfiguration();
        cronTaskConfiguration.setName(name);
        cronTaskConfiguration.addProperty("jobClass", MyTask.class.getName());
        cronTaskConfiguration.addProperty("cronExpression", "0 0/1 * 1/1 * ? *");

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);

        CronTaskConfiguration obj = cronTaskConfigurationService.findOne(name);
        assertNotNull(obj);
    }

    public void updateConfig()
            throws SchedulerException,
                   ClassNotFoundException,
                   CronTaskException,
                   InstantiationException,
                   IllegalAccessException
    {
        String name = "Cron-Task-1";
        CronTaskConfiguration cronTaskConfiguration = cronTaskConfigurationService.findOne(name);

        assertNotNull(cronTaskConfiguration);

        cronTaskConfiguration.addProperty("cronExpression", "0 0 12 1/1 * ? *");

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);
    }

    public void deleteConfig()
            throws SchedulerException, CronTaskNotFoundException, ClassNotFoundException
    {
        String name = "Cron-Task-1";

        cronTaskConfigurationService.getConfiguration(name).forEach(cronTaskConfiguration ->
                                                                    {
                                                                        assertNotNull(cronTaskConfiguration);
                                                                        try
                                                                        {
                                                                            cronTaskConfigurationService.deleteConfiguration(
                                                                                    cronTaskConfiguration);
                                                                        }
                                                                        catch (Exception e)
                                                                        {
                                                                            throw new RuntimeException(e);
                                                                        }
                                                                    });

        assertNull(cronTaskConfigurationService.findOne(name));
    }

}
