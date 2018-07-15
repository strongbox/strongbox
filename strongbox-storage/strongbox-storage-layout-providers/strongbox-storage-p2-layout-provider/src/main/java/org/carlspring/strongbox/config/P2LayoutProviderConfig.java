package org.carlspring.strongbox.config;

import org.carlspring.strongbox.providers.layout.P2LayoutProvider;
import org.carlspring.strongbox.repository.P2RepositoryFeatures;
import org.carlspring.strongbox.repository.P2RepositoryManagementStrategy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.repository",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
               })
public class P2LayoutProviderConfig
{

    @Bean(name = "p2LayoutProvider")
    P2LayoutProvider p2LayoutProvider()
    {
        return new P2LayoutProvider();
    }

    @Bean(name = "p2RepositoryFeatures")
    P2RepositoryFeatures p2RepositoryFeatures()
    {
        return new P2RepositoryFeatures();
    }

    @Bean(name = "p2RepositoryManagementStrategy")
    P2RepositoryManagementStrategy p2RepositoryManagementStrategy()
    {
        return new P2RepositoryManagementStrategy();
    }

}
