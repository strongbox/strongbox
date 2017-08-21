package org.carlspring.strongbox.cron.services.impl;

import org.carlspring.strongbox.cron.jobs.AbstractCronJob;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.exceptions.CronTaskException;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;
import org.carlspring.strongbox.cron.quartz.CronJobSchedulerService;
import org.carlspring.strongbox.cron.quartz.GroovyScriptNames;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.CronTaskDataService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.IteratorUtils;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class CronTaskConfigurationServiceImpl
        implements CronTaskConfigurationService
{

    private final Logger logger = LoggerFactory.getLogger(CronTaskConfigurationServiceImpl.class);

    @Inject
    private CronTaskDataService cronTaskDataService;

    @Inject
    private CronJobSchedulerService cronJobSchedulerService;


    public void saveConfiguration(CronTaskConfiguration cronTaskConfiguration)
            throws ClassNotFoundException,
                   SchedulerException,
                   CronTaskException,
                   IllegalAccessException,
                   InstantiationException
    {
        logger.debug("CronTaskConfigurationService.saveConfiguration()");

        if (!cronTaskConfiguration.contains("cronExpression"))
        {
            throw new CronTaskException("cronExpression property does not exists");
        }

        cronTaskDataService.save(cronTaskConfiguration);

        if (cronTaskConfiguration.contains("jobClass"))
        {
            Class c = Class.forName(cronTaskConfiguration.getProperty("jobClass"));
            Object classInstance = c.newInstance();

            logger.debug("> " + c.getSuperclass().getCanonicalName());

            if (!(classInstance instanceof AbstractCronJob))
            {
                throw new CronTaskException(c + " does not extend " + AbstractCronJob.class);
            }

            cronJobSchedulerService.scheduleJob(cronTaskConfiguration);
        }
    }

    public void deleteConfiguration(CronTaskConfiguration cronTaskConfiguration)
            throws SchedulerException,
                   CronTaskNotFoundException,
                   ClassNotFoundException
    {
        logger.debug("CronTaskConfigurationService.deleteConfiguration()");

        cronTaskDataService.delete(cronTaskConfiguration);
        cronJobSchedulerService.deleteJob(cronTaskConfiguration);
    }

    public List<CronTaskConfiguration> getConfiguration(String name)
    {
        return cronTaskDataService.findByName(name);
    }

    @Override
    public CronTaskConfiguration findOne(String name)
    {
        List<CronTaskConfiguration> configurations = getConfiguration(name);
        if (configurations == null || configurations.isEmpty())
        {
            return null;
        }

        return configurations.get(0);
    }

    public List<CronTaskConfiguration> getConfigurations()
    {
        logger.debug("CronTaskConfigurationService.getConfigurations()");

        Optional<List<CronTaskConfiguration>> optional = cronTaskDataService.findAll();

        return (List<CronTaskConfiguration>) (optional.isPresent() ? optional.get() :
                                              IteratorUtils.toList(optional.get().iterator()));
    }

    public GroovyScriptNames getGroovyScriptsName()
    {
        logger.debug("CronTaskConfigurationService.getGroovyScriptsName");

        return cronJobSchedulerService.getGroovyScriptsName();
    }
}
