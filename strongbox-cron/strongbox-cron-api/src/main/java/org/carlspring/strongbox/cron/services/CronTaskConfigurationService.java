package org.carlspring.strongbox.cron.services;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;

import org.quartz.SchedulerException;

public interface CronTaskConfigurationService
{


    void saveConfiguration(CronTaskConfigurationDto cronTaskConfiguration)
            throws Exception;

    void deleteConfiguration(String cronTaskConfigurationName)
            throws SchedulerException,
                   CronTaskNotFoundException,
                   ClassNotFoundException;

    CronTaskConfigurationDto getTaskConfigurationDto(String cronTaskConfigurationName);

    CronTasksConfigurationDto getTasksConfigurationDto();

}
