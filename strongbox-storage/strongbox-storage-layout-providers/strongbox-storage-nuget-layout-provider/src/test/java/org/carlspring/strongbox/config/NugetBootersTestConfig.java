package org.carlspring.strongbox.config;

import org.carlspring.strongbox.booters.TempDirBooter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * @author kalski
 */
@Configuration
@ComponentScan({ "org.carlspring.strongbox.booters" })
public class NugetBootersTestConfig
{
    @Bean
    TempDirBooter tempDirBooter()
    {
        return new TempDirBooter();
    }
    
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
    {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setIgnoreResourceNotFound(true);

        return propertySourcesPlaceholderConfigurer;
    }
}
