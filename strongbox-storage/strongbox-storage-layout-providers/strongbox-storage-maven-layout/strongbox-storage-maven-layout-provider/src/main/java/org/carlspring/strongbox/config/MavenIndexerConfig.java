package org.carlspring.strongbox.config;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.storage.indexing.SafeArtifactContextProducer;
import org.carlspring.strongbox.storage.indexing.StrongboxIndexer;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.index.*;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Conditional(MavenIndexerEnabledCondition.class)
public class MavenIndexerConfig
{

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
    IndexUpdater indexUpdater()
    {
        return new DefaultIndexUpdater(new DefaultIncrementalHandler(), null);
    }

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
