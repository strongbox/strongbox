package org.carlspring.strongbox.cron.services.impl;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.cron.domain.GroovyScriptNamesDto;
import org.carlspring.strongbox.cron.exceptions.CronTaskException;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;
import org.carlspring.strongbox.cron.jobs.AbstractCronJob;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.CronTaskDataService;
import org.carlspring.strongbox.event.cron.CronTaskEventListenerRegistry;

import javax.inject.Inject;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
class CronTaskConfigurationServiceImpl
        implements CronTaskConfigurationService, ApplicationContextAware
{

    private final Logger logger = LoggerFactory.getLogger(CronTaskConfigurationServiceImpl.class);

    @Inject
    protected CronTaskEventListenerRegistry cronTaskEventListenerRegistry;

    private AutowireCapableBeanFactory beanFactory;

    @Inject
    private CronTaskDataService cronTaskDataService;

    @Inject
    private CronJobSchedulerService cronJobSchedulerService;

    @Override
    public void setApplicationContext(final ApplicationContext context)
    {
        beanFactory = context.getAutowireCapableBeanFactory();
    }

    public void saveConfiguration(CronTaskConfigurationDto configuration)
            throws Exception
    {
        logger.debug("CronTaskConfigurationService.saveConfiguration()");

        if (!configuration.contains("cronExpression"))
        {
            throw new CronTaskException("cronExpression property does not exists");
        }

        cronTaskDataService.save(configuration);

        if (configuration.contains("jobClass"))
        {
            Class c = Class.forName(configuration.getProperty("jobClass"));
            Object classInstance = c.getConstructor().newInstance();

            logger.debug("> " + c.getSuperclass().getCanonicalName());

            if (!(classInstance instanceof AbstractCronJob))
            {
                throw new CronTaskException(c + " does not extend " + AbstractCronJob.class);
            }

            beanFactory.autowireBean(classInstance);
            ((AbstractCronJob) classInstance).beforeScheduleCallback(configuration);

            cronJobSchedulerService.scheduleJob(configuration);

            if (configuration.shouldExecuteImmediately())
            {
                cronJobSchedulerService.executeJob(configuration);
            }
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

    public GroovyScriptNamesDto getGroovyScriptsName()
    {
        logger.debug("CronTaskConfigurationService.getGroovyScriptsName");

        return cronJobSchedulerService.getGroovyScriptsName();
    }

    @Override
    public void setConfiguration(final CronTasksConfigurationDto cronTasksConfiguration)
            throws Exception
    {
        for (CronTaskConfigurationDto dto : cronTasksConfiguration.getCronTaskConfigurations()) {
            saveConfiguration(dto);
        }

    }
}
