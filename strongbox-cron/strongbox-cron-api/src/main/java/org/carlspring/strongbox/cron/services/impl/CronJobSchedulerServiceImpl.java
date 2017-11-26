package org.carlspring.strongbox.cron.services.impl;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.domain.GroovyScriptNames;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;
import org.carlspring.strongbox.cron.quartz.CronTask;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

/**
 * @author Yougeshwar
 */
@Service
public class CronJobSchedulerServiceImpl
        implements CronJobSchedulerService
{

    private final Logger logger = LoggerFactory.getLogger(CronJobSchedulerServiceImpl.class);

    @Inject
    private SchedulerFactoryBean schedulerFactoryBean;

    private Map<String, CronTask> jobsMap = new HashMap<>();


    @Override
    public void scheduleJob(CronTaskConfiguration cronTaskConfiguration)
            throws ClassNotFoundException, SchedulerException
    {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        String cronExpression = cronTaskConfiguration.getProperty("cronExpression");

        if (jobsMap.containsKey(cronTaskConfiguration.getName()))
        {
            CronTask cronTask = jobsMap.get(cronTaskConfiguration.getName());
            JobDetail jobDetail = cronTask.getJobDetail();
            Trigger oldTrigger = cronTask.getTrigger();

            Trigger newTrigger = TriggerBuilder.newTrigger()
                                               .withIdentity(cronTaskConfiguration.getName())
                                               .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                                               .build();

            scheduler.addJob(jobDetail, true, true);
            scheduler.rescheduleJob(oldTrigger.getKey(), newTrigger);

            cronTask.setTrigger(newTrigger);
            cronTask.setScriptName(cronTaskConfiguration.getProperty("fileName"));
        }
        else
        {
            CronTask cronTask = new CronTask();

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("config", cronTaskConfiguration);
            jobDataMap.put("schedulerFactoryBean", schedulerFactoryBean);
            jobDataMap.put("cronTask", cronTask);

            //noinspection unchecked
            Class<? extends Job> jobClass = (Class<? extends Job>) Class.forName(cronTaskConfiguration.getProperty("jobClass"));

            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                                            .withIdentity(cronTaskConfiguration.getName())
                                            .setJobData(jobDataMap).build();

            Trigger trigger = TriggerBuilder.newTrigger()
                                            .withIdentity(cronTaskConfiguration.getName())
                                            .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();

            scheduler.scheduleJob(jobDetail, trigger);

            cronTask.setJobDetail(jobDetail);
            cronTask.setTrigger(trigger);
            cronTask.setScriptName(cronTaskConfiguration.getProperty("fileName"));

            jobsMap.put(cronTaskConfiguration.getName(), cronTask);
        }

        if (!scheduler.isStarted())
        {
            logger.debug("Scheduler started");

            scheduler.start();
        }

        logger.debug("Job '" + cronTaskConfiguration.getName() + "' scheduled.");
    }

    @Override
    public void executeJob(CronTaskConfiguration cronTaskConfiguration)
            throws SchedulerException
    {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        if (jobsMap.containsKey(cronTaskConfiguration.getName()))
        {
            CronTask cronTask = jobsMap.get(cronTaskConfiguration.getName());
            JobDetail jobDetail = cronTask.getJobDetail();

            scheduler.triggerJob(jobDetail.getKey());
        }
        else
        {
            throw new SchedulerException("Could not find cron task with key " + cronTaskConfiguration.getName() + "!");
        }
    }

    @Override
    public void deleteJob(CronTaskConfiguration cronTaskConfiguration)
            throws ClassNotFoundException, SchedulerException, CronTaskNotFoundException
    {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        if (!jobsMap.containsKey(cronTaskConfiguration.getName()))
        {
            logger.warn("Could not find cron task under the specified name! Existing jobs map: {}, lookup name: {}", jobsMap,
                        cronTaskConfiguration.getName());
            return;
        }

        CronTask cronTask = jobsMap.get(cronTaskConfiguration.getName());
        JobDetail jobDetail = cronTask.getJobDetail();
        Trigger trigger = cronTask.getTrigger();

        scheduler.unscheduleJob(trigger.getKey());
        scheduler.deleteJob(jobDetail.getKey());

        jobsMap.remove(cronTaskConfiguration.getName());

        logger.debug("Job '" + cronTaskConfiguration.getName() + "' un-scheduled.");
    }

    @Override
    public GroovyScriptNames getGroovyScriptsName()
    {
        GroovyScriptNames groovyScriptNames = new GroovyScriptNames();
        for (CronTask struct : jobsMap.values())
        {
            if (struct.getScriptName() != null && !struct.getScriptName().isEmpty() &&
                struct.getScriptName().endsWith(".groovy"))
            {
                groovyScriptNames.addName(struct.getScriptName());
            }
        }

        return groovyScriptNames;
    }

}
