package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.CronJobStatusEnum;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.JobManager;
import org.carlspring.strongbox.event.cron.CronTaskEventListenerRegistry;

import javax.inject.Inject;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author carlspring
 */
public abstract class AbstractCronJob
        extends QuartzJobBean
        implements InterruptableJob
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private CronTaskConfiguration configuration;

    @Inject
    private CronTaskEventListenerRegistry cronTaskEventListenerRegistry;

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private JobManager manager;

    private String status = CronJobStatusEnum.SLEEPING.getStatus();


    public abstract void executeTask(CronTaskConfiguration config)
            throws Throwable;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        logger.info("Executing cron job task ...");

        if (configuration == null)
        {
            configuration = cronTaskConfigurationService.findOne(jobExecutionContext.getJobDetail()
                                                                                    .getKey()
                                                                                    .getName());
        }

        setStatus(CronJobStatusEnum.EXECUTING.getStatus());
        cronTaskEventListenerRegistry.dispatchCronTaskExecutingEvent(configuration.getName());

        CronTaskConfiguration config = (CronTaskConfiguration) jobExecutionContext.getMergedJobDataMap().get("config");

        try
        {
            executeTask(config);
            logger.info(String.format("Cron job task [%s] execution completed.", configuration.getName()));
        }
        catch (Throwable e)
        {
            logger.error(String.format("Failed to execute cron job task [%s].", configuration.getName()), e);
        }
        manager.addExecutedJob(config.getName(), true);

        cronTaskEventListenerRegistry.dispatchCronTaskExecutedEvent(configuration.getName());
        setStatus(CronJobStatusEnum.SLEEPING.getStatus());

        if (configuration.isOneTimeExecution())
        {
            try
            {
                cronTaskConfigurationService.deleteConfiguration(getConfiguration());
            }
            catch (Exception e)
            {
                logger.error(String.format("Failed to delete cron job task [%s].", configuration.getName()), e);
            }
        }
    }

    @Override
    public void interrupt()
            throws UnableToInterruptJobException
    {
    }

    public void beforeScheduleCallback(CronTaskConfiguration config)
            throws Exception
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

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

}
