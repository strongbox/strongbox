package org.carlspring.strongbox.cron.config;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.StorageCoreConfig;
import org.carlspring.strongbox.cron.services.impl.CronTaskExecutor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.cron",
                 "org.carlspring.strongbox.event.cron",
                 "org.carlspring.strongbox.dependency.snippet" })
@Import({ DataServiceConfig.class,
          StorageCoreConfig.class
})
public class CronTasksConfig
{

    @Inject
    private ApplicationContext applicationContext;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean()
    {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setJobFactory(springBeanJobFactory());
        schedulerFactoryBean.setTaskExecutor(cronJobTaskExecutor());
        
        return schedulerFactoryBean;
    }
    
    @Bean
    public Executor cronJobTaskExecutor()
    {
        return new CronTaskExecutor(10, 10, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    @Bean
    public SpringBeanJobFactory springBeanJobFactory()
    {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);

        return jobFactory;
    }

}
