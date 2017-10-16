package org.carlspring.strongbox.config;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.NugetHierarchicalArtifactCoordinates;
import org.carlspring.strongbox.providers.layout.NugetHierarchicalLayoutProvider;
import org.carlspring.strongbox.repository.NugetRepositoryFeatures;
import org.carlspring.strongbox.repository.NugetRepositoryManagementStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.orientechnologies.orient.core.entity.OEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.event",
                 "org.carlspring.strongbox.repository",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
               })
@Import({ EventsConfig.class })
public class NugetLayoutProviderConfig
{

    @Inject
    private OEntityManager oEntityManager;
    
    @Inject
    private TransactionTemplate transactionTemplate;
    

    @PostConstruct
    public void init()
    {
        transactionTemplate.execute((s) -> {
            oEntityManager.registerEntityClass(NugetHierarchicalArtifactCoordinates.class);
            return null;
        });
    }
    
    @Bean(name = "nugetHierarchicalLayoutProvider")
    NugetHierarchicalLayoutProvider nugetHierarchicalLayoutProvider()
    {
        return new NugetHierarchicalLayoutProvider();
    }

    @Bean(name = "nugetRepositoryFeatures")
    NugetRepositoryFeatures nugetRepositoryFeatures()
    {
        return new NugetRepositoryFeatures();
    }

    @Bean(name = "nugetRepositoryManagementStrategy")
    NugetRepositoryManagementStrategy nugetRepositoryManagementStrategy()
    {
        return new NugetRepositoryManagementStrategy();
    }

}
