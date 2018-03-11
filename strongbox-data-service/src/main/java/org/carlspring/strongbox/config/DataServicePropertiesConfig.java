package org.carlspring.strongbox.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
public class DataServicePropertiesConfig
{

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
    {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ConnectionConfig connectionConfig()
        throws IOException
    {
        ConnectionConfigOrientDB.bootstrap();

        return new ConnectionConfigOrientDB();
    }
}
