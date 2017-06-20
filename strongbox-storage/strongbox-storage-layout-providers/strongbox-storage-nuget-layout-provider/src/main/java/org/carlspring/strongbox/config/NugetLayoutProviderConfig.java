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

@Configuration
@ComponentScan({ "org.carlspring.strongbox.repository",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
               })
public class NugetLayoutProviderConfig
{

    @Inject
    OEntityManager entityManager;


    @PostConstruct
    public void init()
    {
        entityManager.registerEntityClass(NugetHierarchicalArtifactCoordinates.class);
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
