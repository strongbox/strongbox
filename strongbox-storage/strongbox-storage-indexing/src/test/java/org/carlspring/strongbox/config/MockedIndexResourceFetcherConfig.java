package org.carlspring.strongbox.config;

import org.carlspring.strongbox.storage.indexing.downloader.IndexDownloader;
import org.carlspring.strongbox.storage.indexing.downloader.MockedIndexResourceFetcher;

import org.apache.maven.index.updater.ResourceFetcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.booters",
                 "org.carlspring.strongbox.configuration",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
                 "org.carlspring.strongbox.util" })
public class MockedIndexResourceFetcherConfig
{

    @Bean(name = "indexResourceFetcher")
    ResourceFetcher indexResourceFetcher()
    {
        return new MockedIndexResourceFetcher();
    }

    @Bean(name = "indexDownloader")
    IndexDownloader indexDownloader()
    {
        IndexDownloader indexDownloader = new IndexDownloader();
        indexDownloader.setIndexResourceFetcher(indexResourceFetcher());

        return indexDownloader;
    }

}
