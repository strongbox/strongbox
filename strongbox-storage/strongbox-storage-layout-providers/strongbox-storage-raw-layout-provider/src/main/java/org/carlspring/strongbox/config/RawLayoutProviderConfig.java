package org.carlspring.strongbox.config;

import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.repository.RawRepositoryFeatures;
import org.carlspring.strongbox.repository.RawRepositoryManagementStrategy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.repository",
                 "org.carlspring.strongbox.event",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
})
public class RawLayoutProviderConfig
{

    @Bean(name = "rawLayoutProvider")
    RawLayoutProvider maven2LayoutProvider()
    {
        return new RawLayoutProvider();
    }

    @Bean(name = "rawRepositoryFeatures")
    RawRepositoryFeatures mavenRepositoryFeatures()
    {
        return new RawRepositoryFeatures();
    }

    @Bean(name = "rawRepositoryManagementStrategy")
    RawRepositoryManagementStrategy mavenRepositoryManagementStrategy()
    {
        return new RawRepositoryManagementStrategy();
    }

}
