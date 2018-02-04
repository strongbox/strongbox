package org.carlspring.strongbox.cron.services.impl;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.carlspring.strongbox.log.CronTaskContextFilter;
import org.quartz.JobDetail;
import org.quartz.core.JobRunShell;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.ReflectionUtils;

public class CronTaskExecutor extends ThreadPoolExecutor implements DisposableBean
{

    private static final Logger LOGGER = LoggerFactory.getLogger(CronTaskExecutor.class);

    public CronTaskExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            BlockingQueue<Runnable> workQueue,
                            RejectedExecutionHandler handler)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public CronTaskExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            BlockingQueue<Runnable> workQueue,
                            ThreadFactory threadFactory,
                            RejectedExecutionHandler handler)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public CronTaskExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            BlockingQueue<Runnable> workQueue,
                            ThreadFactory threadFactory)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public CronTaskExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            BlockingQueue<Runnable> workQueue)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    public void destroy()
        throws Exception
    {
        shutdown();
    }

    @Override
    protected void beforeExecute(Thread t,
                                 Runnable r)
    {
        try
        {
            JobDetail jobDetails = exposeJobDetails(r);
            Optional.ofNullable(jobDetails).ifPresent(this::bootstrapCronJobContext);
        }
        catch (Exception e)
        {
            LOGGER.error(String.format("Before execute failed for [%s]", r), e);
        }
    }

    private void bootstrapCronJobContext(JobDetail jd)
    {
        String jobClassName = jd.getJobClass().getSimpleName();
        LOGGER.debug(String.format("Bootstrap Cron Job [%s]", jobClassName));
        MDC.put(CronTaskContextFilter.STRONGBOX_CRON_CONTEXT_NAME, jobClassName);
    }

    private JobDetail exposeJobDetails(Runnable r)
    {
        Field firedTriggerBundleField = ReflectionUtils.findField(JobRunShell.class, "firedTriggerBundle");
        firedTriggerBundleField.setAccessible(true);

        try
        {
            return ((TriggerFiredBundle) firedTriggerBundleField.get(r)).getJobDetail();
        }
        catch (Exception e)
        {
            LOGGER.error(String.format("Failed to expose Cron Job details for [%s] class",
                                       r.getClass().getSimpleName()),
                         e);
        }
        return null;
    }

    @Override
    protected void afterExecute(Runnable r,
                                Throwable t)
    {
        try
        {
            JobDetail jobDetails = exposeJobDetails(r);
            Optional.ofNullable(jobDetails).ifPresent(this::clearCronJobContext);
        }
        catch (Exception e)
        {
            LOGGER.error(String.format("After execute failed for [%s]", r), e);
        }
    }

    private void clearCronJobContext(JobDetail jd)
    {
        MDC.remove(CronTaskContextFilter.STRONGBOX_CRON_CONTEXT_NAME);
    }

}
