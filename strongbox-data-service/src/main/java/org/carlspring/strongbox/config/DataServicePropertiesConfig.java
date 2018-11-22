package org.carlspring.strongbox.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataServicePropertiesConfig
{

    @Bean
    public ConnectionConfig connectionConfig()
        throws IOException
    {
        ConnectionConfigOrientDB.bootstrap();

        return new ConnectionConfigOrientDB();
    }
}
