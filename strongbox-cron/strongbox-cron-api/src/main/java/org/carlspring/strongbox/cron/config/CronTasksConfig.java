package org.carlspring.strongbox.cron.config;

import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.StorageCoreConfig;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.TransactionManager;

import com.orientechnologies.orient.core.entity.OEntityManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.cron", "org.carlspring.strongbox.dependency.snippet" })
@Import({ DataServiceConfig.class,
          StorageCoreConfig.class
})
public class CronTasksConfig
{

    @Inject
    private OEntityManager oEntityManager;

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private TransactionTemplate transactionTemplate;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean()
    {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setJobFactory(springBeanJobFactory());

        return schedulerFactoryBean;
    }

    @Bean
    public SpringBeanJobFactory springBeanJobFactory()
    {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);

        return jobFactory;
    }

    @PostConstruct
    public void init()
    {
        transactionTemplate.execute((s) ->
        {
            oEntityManager.registerEntityClass(CronTaskConfiguration.class);
            return null;
        });
    }

}
