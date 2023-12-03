package org.carlspring.strongbox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * @author korest
 */
@Configuration
@ComponentScan({ "org.carlspring.strongbox.service.impl",
                 "org.carlspring.strongbox.client" })
@PropertySource(value = { "classpath:META-INF/properties/strongbox-client.properties" })
public class ClientConfig
{

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
    {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setIgnoreResourceNotFound(true);

        return propertySourcesPlaceholderConfigurer;
    }

}
