package org.carlspring.strongbox.cron.services.impl;

import javax.inject.Inject;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.cron.exceptions.CronTaskException;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;
import org.carlspring.strongbox.cron.jobs.AbstractCronJob;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.CronTaskDataService;
import org.carlspring.strongbox.event.cron.CronTaskEventListenerRegistry;
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
            
            if (dto.shouldExecuteImmediately())
            {
                cronJobSchedulerService.executeJob(dto);
            }
        }
    }

    public void saveConfiguration(CronTaskConfigurationDto configuration)
        throws Exception
    {
        logger.debug("CronTaskConfigurationService.saveConfiguration()");

        Class c = Class.forName(configuration.getProperty("jobClass"));
        Object classInstance = c.newInstance();

        logger.debug("> " + c.getSuperclass().getCanonicalName());

        if (!(classInstance instanceof AbstractCronJob))
        {
            throw new CronTaskException(c + " does not extend " + AbstractCronJob.class);
        }

        if (!configuration.isOneTimeExecution())
        {
            cronTaskDataService.save(configuration);
        }   

        cronJobSchedulerService.scheduleJob(configuration);
        if (configuration.isOneTimeExecution() || configuration.shouldExecuteImmediately())
        {
			cronJobSchedulerService.executeJob(configuration);
        }

        cronTaskEventListenerRegistry.dispatchCronTaskCreatedEvent(configuration.getName());
    }

    public void deleteConfiguration(String cronTaskConfigurationName)
        throws SchedulerException,
        CronTaskNotFoundException,
        ClassNotFoundException
    {
        logger.debug("Deleting cron task configuration {}", cronTaskConfigurationName);

        cronTaskDataService.delete(cronTaskConfigurationName);
        cronJobSchedulerService.deleteJob(cronTaskConfigurationName);

        cronTaskEventListenerRegistry.dispatchCronTaskDeletedEvent(cronTaskConfigurationName);
    }

    @Override
    public CronTaskConfigurationDto getTaskConfigurationDto(String name)
    {
        return cronTaskDataService.getTaskConfigurationDto(name);
    }

    public CronTasksConfigurationDto getTasksConfigurationDto()
    {
        return cronTaskDataService.getTasksConfigurationDto();
    }

}
