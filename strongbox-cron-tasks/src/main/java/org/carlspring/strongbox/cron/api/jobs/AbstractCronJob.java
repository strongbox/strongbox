package org.carlspring.strongbox.cron.api.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.quartz.CronTask;

import org.quartz.InterruptableJob;
import org.quartz.UnableToInterruptJobException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * @author carlspring
 */
public abstract class AbstractCronJob
        extends QuartzJobBean
        implements InterruptableJob
{

    private CronTaskConfiguration configuration = new CronTaskConfiguration();

    private SchedulerFactoryBean schedulerFactoryBean;

    private CronTask cronTask;


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

    @Override
    public void interrupt()
            throws UnableToInterruptJobException
    {

    }
}
