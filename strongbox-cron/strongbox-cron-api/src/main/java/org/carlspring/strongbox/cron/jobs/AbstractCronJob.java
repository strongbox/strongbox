package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.CronJobStatusEnum;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;
import org.carlspring.strongbox.cron.quartz.CronTask;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.event.cron.CronTaskEventListenerRegistry;

import javax.inject.Inject;

import org.quartz.*;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * @author carlspring
 */
public abstract class AbstractCronJob
        extends QuartzJobBean
        implements InterruptableJob
{

    private CronTaskConfiguration configuration;

    private SchedulerFactoryBean schedulerFactoryBean;

    @Inject
    private CronTaskEventListenerRegistry cronTaskEventListenerRegistry;

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    private CronTask cronTask;

    private String status = CronJobStatusEnum.SLEEPING.getStatus();


    public abstract void executeTask(JobExecutionContext jobExecutionContext)
            throws JobExecutionException;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        if (configuration == null)
        {
            configuration = cronTaskConfigurationService.findOne(jobExecutionContext.getJobDetail()
                                                                                    .getKey()
                                                                                    .getName());
        }

        setStatus(CronJobStatusEnum.EXECUTING.getStatus());
        cronTaskEventListenerRegistry.dispatchCronTaskExecutingEvent(configuration.getName());

        executeTask(jobExecutionContext);

        cronTaskEventListenerRegistry.dispatchCronTaskExecutedEvent(configuration.getName());
        setStatus(CronJobStatusEnum.SLEEPING.getStatus());

        if (configuration.isOneTimeExecution())
        {
            try
            {
                cronTaskConfigurationService.deleteConfiguration(getConfiguration());
            }
            catch (SchedulerException | CronTaskNotFoundException | ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void interrupt()
            throws UnableToInterruptJobException
    {
    }

    public CronTaskConfiguration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(CronTaskConfiguration configuration)
    {
        this.configuration = configuration;
    }

    public SchedulerFactoryBean getSchedulerFactoryBean()
    {
        return schedulerFactoryBean;
    }

    public void setSchedulerFactoryBean(SchedulerFactoryBean schedulerFactoryBean)
    {
        this.schedulerFactoryBean = schedulerFactoryBean;
    }

    public CronTask getCronTask()
    {
        return cronTask;
    }

    public void setCronTask(CronTask cronTask)
    {
        this.cronTask = cronTask;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

}
