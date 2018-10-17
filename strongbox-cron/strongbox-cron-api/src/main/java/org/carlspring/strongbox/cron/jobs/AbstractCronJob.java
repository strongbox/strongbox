package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.CronJobStatusEnum;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.JobManager;
import org.carlspring.strongbox.event.cron.CronTaskEventListenerRegistry;

import javax.inject.Inject;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author carlspring
 */
@DisallowConcurrentExecution
public abstract class AbstractCronJob
        extends QuartzJobBean
        implements InterruptableJob
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private CronTaskEventListenerRegistry cronTaskEventListenerRegistry;

    @Inject
    private JobManager manager;

    @Inject
    private Environment environment;

    @Inject
    protected CronTaskConfigurationService cronTaskConfigurationService;

    private String status = CronJobStatusEnum.SLEEPING.getStatus();

    public abstract void executeTask(CronTaskConfigurationDto config)
        throws Throwable;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
        throws JobExecutionException
    {

        String jobKey = jobExecutionContext.getJobDetail().getKey().getName();
        
        CronTaskConfigurationDto configuration = cronTaskConfigurationService.getTaskConfigurationDto(jobKey);
        
        if (configuration == null)
        {
            configuration = (CronTaskConfigurationDto) jobExecutionContext.getJobDetail().getJobDataMap().get("config");
        }
        if (configuration == null)
        {
            logger.info(String.format("Configuration not found for [%s].", jobKey));

            return;
        }

        if (!enabled(configuration, environment))
        {
            logger.info(String.format("Cron job [%s] disabled, skip execution.", configuration.getName()));

            return;
        }

        logger.info(String.format("Cron job [%s] enabled, executing.", configuration.getName()));

        setStatus(CronJobStatusEnum.EXECUTING.getStatus());
        cronTaskEventListenerRegistry.dispatchCronTaskExecutingEvent(configuration.getName());

        try
        {
            executeTask(configuration);
            logger.info(String.format("Cron job task [%s] execution completed.", configuration.getName()));
        }
        catch (Throwable e)
        {
            logger.error(String.format("Failed to execute cron job task [%s].", configuration.getName()), e);
        }
        manager.addExecutedJob(configuration.getName(), true);

        cronTaskEventListenerRegistry.dispatchCronTaskExecutedEvent(configuration.getName());
        setStatus(CronJobStatusEnum.SLEEPING.getStatus());

    }

    @Override
    public void interrupt()
        throws UnableToInterruptJobException
    {
    }

    public boolean enabled(CronTaskConfigurationDto configuration,
                           Environment env)
    {
        return configuration.isOneTimeExecution() || !env.acceptsProfiles("test");
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
