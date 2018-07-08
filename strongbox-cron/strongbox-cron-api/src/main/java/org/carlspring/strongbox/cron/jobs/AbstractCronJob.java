package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.CronJobStatusEnum;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
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

    protected CronTaskConfigurationDto configuration;

    @Inject
    private CronTaskEventListenerRegistry cronTaskEventListenerRegistry;

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private JobManager manager;

    private String status = CronJobStatusEnum.SLEEPING.getStatus();


    public abstract void executeTask(CronTaskConfigurationDto config)
            throws Throwable;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        logger.info("Executing cron job task ...");

        if (configuration == null)
        {
            configuration = cronTaskConfigurationService.getTaskConfigurationDto(jobExecutionContext.getJobDetail()
                                                                                                    .getKey()
                                                                                                    .getName());
        }

        setStatus(CronJobStatusEnum.EXECUTING.getStatus());
        cronTaskEventListenerRegistry.dispatchCronTaskExecutingEvent(configuration.getName());

        CronTaskConfigurationDto config = (CronTaskConfigurationDto) jobExecutionContext.getMergedJobDataMap().get("config");

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
                cronTaskConfigurationService.deleteConfiguration(configuration.getName());
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

    public void beforeScheduleCallback(CronTaskConfigurationDto config)
            throws Exception
    {
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
