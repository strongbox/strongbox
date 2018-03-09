package org.carlspring.strongbox.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.dependency.snippet",
                 "org.carlspring.strongbox.event",
                 "org.carlspring.strongbox.repository",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
})
@Import({ EventsConfig.class })
public class NpmLayoutProviderConfig
{

    @Bean
    public ObjectMapper npmJackasonMapper()
    {
        return new ObjectMapper();
    }
    
}
