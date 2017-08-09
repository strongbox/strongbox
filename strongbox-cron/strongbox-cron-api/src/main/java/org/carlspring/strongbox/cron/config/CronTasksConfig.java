package org.carlspring.strongbox.cron.config;

import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.StorageCoreConfig;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientechnologies.orient.core.entity.OEntityManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.cron" })
@Import({ DataServiceConfig.class,
          StorageCoreConfig.class
        })
public class CronTasksConfig
{

    @Inject
    private OEntityManager oEntityManager;

    @Inject
    private ApplicationContext applicationContext;


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
    @Transactional
    public void init()
    {
        // register all domain entities
        oEntityManager.registerEntityClasses(CronTaskConfiguration.class.getPackage().getName());
    }

    @Bean
    public ObjectMapper objectMapper()
    {
        return new ObjectMapper();
    }

    @Bean
    public Jaxb2RootElementHttpMessageConverter jaxbConverter()
    {
        return new Jaxb2RootElementHttpMessageConverter();
    }

}
