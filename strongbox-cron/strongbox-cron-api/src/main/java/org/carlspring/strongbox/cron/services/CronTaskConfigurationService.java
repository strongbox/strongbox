package org.carlspring.strongbox.cron.services;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.exceptions.CronTaskException;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;
import org.carlspring.strongbox.cron.domain.GroovyScriptNames;

import java.util.List;

import org.quartz.SchedulerException;

public interface CronTaskConfigurationService
{


    void saveConfiguration(CronTaskConfiguration cronTaskConfiguration)
            throws ClassNotFoundException,
                   SchedulerException,
                   CronTaskException,
                   IllegalAccessException,
                   InstantiationException;

    void deleteConfiguration(CronTaskConfiguration cronTaskConfiguration)
            throws SchedulerException,
                   CronTaskNotFoundException,
                   ClassNotFoundException;

    List<CronTaskConfiguration> getConfiguration(String name);

    CronTaskConfiguration findOne(String name);

    List<CronTaskConfiguration> getConfigurations();

    GroovyScriptNames getGroovyScriptsName();
}
