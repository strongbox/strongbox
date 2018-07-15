package org.carlspring.strongbox.config;

import javax.servlet.ServletContext;

import org.carlspring.strongbox.event.EventExecutorFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.event" })
@EnableAsync
public class EventsConfig
{

    @Bean
    public EventExecutorFactoryBean eventTaskExecutor(@Autowired(required = false) ServletContext servletContext)
    {
        return new EventExecutorFactoryBean(servletContext);
    }

}
