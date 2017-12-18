package org.carlspring.strongbox.config;

import org.carlspring.strongbox.artifact.coordinates.NullArtifactCoordinates;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.repository.RawRepositoryFeatures;
import org.carlspring.strongbox.repository.RawRepositoryManagementStrategy;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.orientechnologies.orient.core.entity.OEntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.repository",
                 "org.carlspring.strongbox.event",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
})
public class RawLayoutProviderConfig
{

    @Inject
    private TransactionTemplate transactionTemplate;
    
    @Inject
    private OEntityManager oEntityManager;

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

    @PostConstruct
    public void init()
    {
        transactionTemplate.execute((s) -> {
            oEntityManager.registerEntityClass(NullArtifactCoordinates.class);
            return null;
        });
    }

    @Bean(name = "rawRepositoryManagementStrategy")
    RawRepositoryManagementStrategy mavenRepositoryManagementStrategy()
    {
        return new RawRepositoryManagementStrategy();
    }

}
