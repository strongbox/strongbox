package org.carlspring.strongbox.cron.services.impl;

import java.util.Collections;

import javax.inject.Inject;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Yougeshwar
 */
@Service
public class CronJobSchedulerServiceImpl
        implements CronJobSchedulerService
{

    private static final Logger logger = LoggerFactory.getLogger(CronJobSchedulerServiceImpl.class);

    @Inject
    private Scheduler scheduler;

    @Override
    public void scheduleJob(CronTaskConfigurationDto cronTaskConfiguration)
    {
        String jobClassName = cronTaskConfiguration.getProperty("jobClass");
        Class<? extends Job> jobClass;
        try
        {
            jobClass = (Class<? extends Job>) Class.forName(jobClassName);
        }
        catch (ClassNotFoundException e1)
        {
            logger.error(String.format("Failed to shcedule cron job [%s]", jobClassName));

            return;
        }

        String cronExpression = cronTaskConfiguration.getProperty("cronExpression");
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("config", cronTaskConfiguration);
        
        JobKey jobKey = JobKey.jobKey(cronTaskConfiguration.getName());
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                                        .withIdentity(jobKey)
                                        .setJobData(jobDataMap)
                                        .build();

        TriggerKey triggerKey = TriggerKey.triggerKey(cronTaskConfiguration.getName());
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                                                               .withIdentity(triggerKey)
                                                               .forJob(jobDetail);
        if (cronTaskConfiguration.isOneTimeExecution())
        {
            triggerBuilder.startNow();
        }
        else
        {
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression));
        }
        Trigger trigger = triggerBuilder.build();

        try
        {
            scheduler.scheduleJob(jobDetail, Collections.singleton(trigger), true);
        }
        catch (SchedulerException e)
        {
            logger.error(String.format("Failed to add Cron Job:%n [%s]", cronTaskConfiguration), e);

            return;
        }

        logger.debug("Job '" + cronTaskConfiguration.getName() + "' scheduled.");
    }

    @Override
    public void executeJob(CronTaskConfigurationDto cronTaskConfiguration)
    {
        JobKey jobKey = JobKey.jobKey(cronTaskConfiguration.getName());

        try
        {
            scheduler.triggerJob(jobKey);
        }
        catch (SchedulerException e)
        {
            logger.error(String.format("Failed to shcedule cron job [%s]", jobKey));
        }
    }

    @Override
    public void deleteJob(String cronTaskConfigurationName)
    {
        JobKey jobKey = JobKey.jobKey(cronTaskConfigurationName);

        try
        {
            scheduler.deleteJob(jobKey);
        }
        catch (SchedulerException e)
        {
            logger.error(String.format("Failed to delete cron job [%s]", jobKey));
        }

        logger.debug("Job '" + cronTaskConfigurationName + "' un-scheduled.");
    }

}
