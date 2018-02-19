package org.carlspring.strongbox.config;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.maven.index.*;
import org.apache.maven.index.Scanner;
import org.apache.maven.index.artifact.ArtifactPackagingMapper;
import org.apache.maven.index.artifact.DefaultArtifactPackagingMapper;
import org.apache.maven.index.creator.AbstractIndexCreator;
import org.apache.maven.index.creator.JarFileContentsIndexCreator;
import org.apache.maven.index.creator.MavenPluginArtifactInfoIndexCreator;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;
import org.apache.maven.index.incremental.DefaultIncrementalHandler;
import org.apache.maven.index.packer.DefaultIndexPacker;
import org.apache.maven.index.packer.IndexPacker;
import org.apache.maven.index.updater.DefaultIndexUpdater;
import org.apache.maven.index.updater.IndexUpdater;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.repository.MavenRepositoryManagementStrategy;
import org.carlspring.strongbox.storage.indexing.SafeArtifactContextProducer;
import org.carlspring.strongbox.storage.indexing.StrongboxIndexer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.support.TransactionTemplate;

import com.orientechnologies.orient.core.entity.OEntityManager;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.configuration",
                 "org.carlspring.strongbox.event",
                 "org.carlspring.strongbox.repository",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
})
public class Maven2LayoutProviderConfig
{

    @Inject
    private TransactionTemplate transactionTemplate;
    
    @Inject
    private OEntityManager oEntityManager;
    
    @Bean(name = "indexer")
    Indexer indexer()
    {
        return new StrongboxIndexer(searchEngine(), indexerEngine(), queryCreator());
    }

    @Bean(name = "scanner")
    Scanner scanner()
    {
        return new DefaultScanner(new DefaultArtifactContextProducer(artifactPackagingMapper()));
    }

    @Bean(name = "indexPacker")
    IndexPacker indexPacker()
    {
        return new DefaultIndexPacker(new DefaultIncrementalHandler());
    }

    @Bean(name = "indexUpdater")
    IndexUpdater indexUpdater() { return new DefaultIndexUpdater(new DefaultIncrementalHandler(), null); }

    @Bean(name = "searchEngine")
    SearchEngine searchEngine()
    {
        return new DefaultSearchEngine();
    }

    @Bean(name = "indexerEngine")
    IndexerEngine indexerEngine()
    {
        return new DefaultIndexerEngine();
    }

    @Bean(name = "queryCreator")
    QueryCreator queryCreator()
    {
        return new DefaultQueryCreator();
    }

    @Bean(name = "mavenIndexerSearchProvider")
    MavenIndexerSearchProvider mavenIndexerSearchProvider()
    {
        return new MavenIndexerSearchProvider();
    }

    @Bean(name = "maven2LayoutProvider")
    Maven2LayoutProvider maven2LayoutProvider()
    {
        return new Maven2LayoutProvider();
    }


    @PostConstruct
    public void init()
    {
        transactionTemplate.execute((s) -> {
            oEntityManager.registerEntityClass(MavenArtifactCoordinates.class);
            return null;
        });
    }

    @Bean(name = "mavenRepositoryManagementStrategy")
    MavenRepositoryManagementStrategy mavenRepositoryManagementStrategy()
    {
        return new MavenRepositoryManagementStrategy();
    }

    @Bean(name = "artifactContextProducer")
    @Scope("prototype")
    ArtifactContextProducer artifactContextProducer(final RepositoryPath artifactPath)
    {
        return new SafeArtifactContextProducer(artifactPackagingMapper(), artifactPath);
    }

    @Bean(name = "artifactPackagingMapper")
    ArtifactPackagingMapper artifactPackagingMapper()
    {
        return new DefaultArtifactPackagingMapper();
    }

    @Bean
    MinimalArtifactInfoIndexCreator minimalArtifactInfoIndexCreator()
    {
        return new MinimalArtifactInfoIndexCreator();
    }

    @Bean
    JarFileContentsIndexCreator jarFileContentsIndexCreator()
    {
        return new JarFileContentsIndexCreator();
    }

    @Bean
    MavenPluginArtifactInfoIndexCreator mavenPluginArtifactInfoIndexCreator()
    {
        return new MavenPluginArtifactInfoIndexCreator();
    }

    @Bean(name = "indexers")
    Map<String, AbstractIndexCreator> indexers()
    {
        LinkedHashMap<String, AbstractIndexCreator> indexers = new LinkedHashMap<>();
        indexers.put("min", minimalArtifactInfoIndexCreator());
        indexers.put("jarContent", jarFileContentsIndexCreator());
        indexers.put("maven-plugin", mavenPluginArtifactInfoIndexCreator());

        return indexers;
    }

}
