package org.carlspring.strongbox.cron.config;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.StorageCoreConfig;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.cron" })
@Import({ DataServiceConfig.class,
          StorageCoreConfig.class
        })
@EnableWebMvc
public class CronTasksConfig
        extends WebMvcConfigurerAdapter
{

    @Inject
    private OObjectDatabaseTx databaseTx;

    @Inject
    ApplicationContext applicationContext;


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
        databaseTx.activateOnCurrentThread();
        databaseTx.getEntityManager().registerEntityClasses(CronTaskConfiguration.class.getPackage().getName());
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
    {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
        stringConverter.setWriteAcceptCharset(false);

        // if your argument is a byte[]
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(stringConverter);
        converters.add(new FormHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter());
        converters.add(jaxbConverter());
        converters.add(new ResourceHttpMessageConverter());
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
