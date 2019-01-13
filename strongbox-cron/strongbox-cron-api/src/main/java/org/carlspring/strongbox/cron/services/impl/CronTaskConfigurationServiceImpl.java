package org.carlspring.strongbox.cron.services.impl;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.CronTaskDataService;
import org.carlspring.strongbox.event.cron.CronTaskEventListenerRegistry;

import javax.inject.Inject;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Service;

@Service
class CronTaskConfigurationServiceImpl
        implements CronTaskConfigurationService, ApplicationListener<ContextStartedEvent>
{

    private final Logger logger = LoggerFactory.getLogger(CronTaskConfigurationServiceImpl.class);

    @Inject
    protected CronTaskEventListenerRegistry cronTaskEventListenerRegistry;

    @Inject
    private CronTaskDataService cronTaskDataService;

    @Inject
    private CronJobSchedulerService cronJobSchedulerService;

    @Override
    public void onApplicationEvent(ContextStartedEvent event)
    {
        CronTasksConfigurationDto cronTasksConfiguration = getTasksConfigurationDto();

        for (CronTaskConfigurationDto dto : cronTasksConfiguration.getCronTaskConfigurations())
        {
            if (dto.isOneTimeExecution())
            {
                continue;
            }
            
            cronJobSchedulerService.scheduleJob(dto);
        }
    }

    public void saveConfiguration(CronTaskConfigurationDto configuration)
        throws Exception
    {
        logger.debug("CronTaskConfigurationService.saveConfiguration()");

        //TODO: validate configuration properties
        
        cronTaskDataService.save(configuration);

        if (configuration.contains("jobClass"))
        {
            cronJobSchedulerService.scheduleJob(configuration);
        }

        cronTaskEventListenerRegistry.dispatchCronTaskCreatedEvent(configuration.getUuid());
    }

    public void deleteConfiguration(String cronTaskConfigurationUuid)
            throws SchedulerException,
                   CronTaskNotFoundException,
                   ClassNotFoundException
    {
        logger.debug("Deleting cron task configuration {}", cronTaskConfigurationUuid);

        cronTaskDataService.delete(cronTaskConfigurationUuid);
        cronJobSchedulerService.deleteJob(cronTaskConfigurationUuid);

        cronTaskEventListenerRegistry.dispatchCronTaskDeletedEvent(cronTaskConfigurationUuid);
    }

    @Override
    public CronTaskConfigurationDto getTaskConfigurationDto(String uuid)
    {
        return cronTaskDataService.getTaskConfigurationDto(uuid);
    }

    public CronTasksConfigurationDto getTasksConfigurationDto()
    {
        return cronTaskDataService.getTasksConfigurationDto();
    }

}
