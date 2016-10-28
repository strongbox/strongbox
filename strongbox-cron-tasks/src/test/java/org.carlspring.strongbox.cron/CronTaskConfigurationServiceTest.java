package org.carlspring.strongbox.cron;

import org.carlspring.strongbox.cron.config.CronTasksConfig;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.exceptions.CronTaskException;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CronTasksConfig.class })
@WebAppConfiguration
public class CronTaskConfigurationServiceTest
{

    @Autowired
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

        CronTaskConfiguration obj = cronTaskConfigurationService.getConfiguration(name);
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
        CronTaskConfiguration cronTaskConfiguration = cronTaskConfigurationService.getConfiguration(name);

        assertNotNull(cronTaskConfiguration);

        cronTaskConfiguration.addProperty("cronExpression", "0 0 12 1/1 * ? *");

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);
    }

    public void deleteConfig()
            throws SchedulerException, CronTaskNotFoundException, ClassNotFoundException
    {
        String name = "Cron-Task-1";
        CronTaskConfiguration cronTaskConfiguration = cronTaskConfigurationService.getConfiguration(name);

        assertNotNull(cronTaskConfiguration);

        cronTaskConfigurationService.deleteConfiguration(cronTaskConfiguration);

        cronTaskConfiguration = cronTaskConfigurationService.getConfiguration(name);
        assertNull(cronTaskConfiguration);
    }

}
