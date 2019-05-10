package org.carlspring.strongbox.cron.services;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;

import java.util.UUID;

import org.quartz.SchedulerException;

public interface CronTaskConfigurationService
{


    UUID saveConfiguration(CronTaskConfigurationDto cronTaskConfiguration)
            throws Exception;

    void deleteConfiguration(String cronTaskConfigurationUuid)
            throws SchedulerException,
                   CronTaskNotFoundException,
                   ClassNotFoundException;

    CronTaskConfigurationDto getTaskConfigurationDto(String cronTaskConfigurationUuid);

    CronTasksConfigurationDto getTasksConfigurationDto();

}
