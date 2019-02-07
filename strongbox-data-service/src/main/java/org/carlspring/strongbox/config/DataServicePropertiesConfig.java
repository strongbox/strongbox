package org.carlspring.strongbox.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DataServicePropertiesConfig
{

    @Bean
    public OrientDBProfile connectionConfig(Environment environment)
        throws IOException
    {
        OrientDBProfile.bootstrap();

        return OrientDBProfile.resolveProfile(environment);
    }
}
