package org.carlspring.strongbox.config;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.maven.index.ArtifactContextProducer;
import org.apache.maven.index.DefaultArtifactContextProducer;
import org.apache.maven.index.DefaultIndexerEngine;
import org.apache.maven.index.DefaultQueryCreator;
import org.apache.maven.index.DefaultScanner;
import org.apache.maven.index.DefaultSearchEngine;
import org.apache.maven.index.Indexer;
import org.apache.maven.index.IndexerEngine;
import org.apache.maven.index.QueryCreator;
import org.apache.maven.index.Scanner;
import org.apache.maven.index.SearchEngine;
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
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.repository.MavenRepositoryManagementStrategy;
import org.carlspring.strongbox.storage.indexing.StrongboxIndexer;
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
public class Maven2LayoutProviderConfig
{

    @Bean(name = "indexer")
    Indexer indexer()
    {
        return new StrongboxIndexer(searchEngine(), indexerEngine(), queryCreator());
    }

    @Bean(name = "scanner")
    Scanner scanner()
    {
        return new DefaultScanner(artifactContextProducer());
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

    @Bean(name = "mavenRepositoryFeatures")
    MavenRepositoryFeatures mavenRepositoryFeatures()
    {
        return new MavenRepositoryFeatures();
    }

    @Inject
    OEntityManager entityManager;


    @PostConstruct
    public void init()
    {
        // unable to replace with more generic one (ArtifactCoordinates) because of
        // internal OrientDB exception: MavenArtifactCoordinates will not be serializable because
        // it was not registered using registerEntityClass()
        entityManager.registerEntityClass(MavenArtifactCoordinates.class);
    }

    @Bean(name = "mavenRepositoryManagementStrategy")
    MavenRepositoryManagementStrategy mavenRepositoryManagementStrategy()
    {
        return new MavenRepositoryManagementStrategy();
    }

    @Bean(name = "artifactContextProducer")
    ArtifactContextProducer artifactContextProducer()
    {
        return new DefaultArtifactContextProducer(artifactPackagingMapper());
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
