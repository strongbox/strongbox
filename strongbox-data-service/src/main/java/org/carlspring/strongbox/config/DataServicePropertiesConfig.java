package org.carlspring.strongbox.config;

import java.io.IOException;

import org.carlspring.strongbox.config.orientdb.OrientDbProfile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DataServicePropertiesConfig
{

    @Bean
    public OrientDbProfile connectionConfig(Environment environment)
        throws IOException
    {
        OrientDbProfile.bootstrap();

        return OrientDbProfile.resolveProfile(environment);
    }
}
