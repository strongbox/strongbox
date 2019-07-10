package org.carlspring.strongbox.cron.services;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;

import java.io.IOException;
import java.util.UUID;

import org.quartz.SchedulerException;

/**
 * @author Pablo Tirado
 */
public interface CronTaskConfigurationService
{


    UUID saveConfiguration(CronTaskConfigurationDto cronTaskConfiguration)
            throws Exception;

    void deleteConfiguration(UUID cronTaskConfigurationUuid)
            throws SchedulerException,
                   CronTaskNotFoundException,
                   ClassNotFoundException, IOException;

    CronTaskConfigurationDto getTaskConfigurationDto(UUID cronTaskConfigurationUuid);

    CronTasksConfigurationDto getTasksConfigurationDto();

}
