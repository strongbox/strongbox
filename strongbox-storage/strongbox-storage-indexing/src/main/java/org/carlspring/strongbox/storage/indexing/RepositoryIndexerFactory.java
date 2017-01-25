package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.RepositoryInitializationException;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.maven.index.Indexer;
import org.apache.maven.index.Scanner;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("repositoryIndexerFactory")
public class RepositoryIndexerFactory
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryIndexerFactory.class);

    private IndexerConfiguration indexerConfiguration;

    @Inject
    private ArtifactIndexesService artifactIndexesService;

    private Configuration configuration;


    @Inject
    public RepositoryIndexerFactory(IndexerConfiguration indexerConfiguration,
                                    ConfigurationManager configurationManager)
    {
        this.indexerConfiguration = indexerConfiguration;
        this.configuration = configurationManager.getConfiguration();
    }

    public RepositoryIndexer createRepositoryIndexer(String storageId,
                                                     String repositoryId,
                                                     String indexType,
                                                     File repositoryBasedir,
                                                     File indexDir)
            throws RepositoryInitializationException
    {
        IndexingContext indexingContext;
        try
        {
            indexingContext = createIndexingContext(storageId, repositoryId, indexType, repositoryBasedir, indexDir);
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);

            throw new RepositoryInitializationException(e.getMessage(), e);
        }

        RepositoryIndexer repositoryIndexer = new RepositoryIndexer(storageId + ":" + repositoryId + ":" + indexType);
        repositoryIndexer.setStorageId(storageId);
        repositoryIndexer.setRepositoryId(repositoryId);
        repositoryIndexer.setRepositoryBasedir(repositoryBasedir);
        repositoryIndexer.setIndexDir(indexDir);
        repositoryIndexer.setIndexingContext(indexingContext);
        repositoryIndexer.setIndexer(indexerConfiguration.getIndexer());
        repositoryIndexer.setScanner(indexerConfiguration.getScanner());
        repositoryIndexer.setConfiguration(configuration);

        return repositoryIndexer;
    }

    private IndexingContext createIndexingContext(String storageId,
                                                  String repositoryId,
                                                  String indexType,
                                                  File repositoryBasedir,
                                                  File indexDir)
            throws IOException
    {
        return getIndexer().createIndexingContext(storageId + ":" + repositoryId + ":" + indexType,
                                                  repositoryId,
                                                  repositoryBasedir,
                                                  indexDir,
                                                  null,
                                                  null,
                                                  true, // if context should be searched in non-targeted mode.
                                                  true, // if indexDirectory is known to contain (or should contain)
                                                        // valid Maven Indexer lucene index, and no checks needed to be
                                                        // performed, or, if we want to "stomp" over existing index
                                                        // (unsafe to do!).
                                                  indexerConfiguration.getIndexersAsList());
    }

    public IndexerConfiguration getIndexerConfiguration()
    {
        return indexerConfiguration;
    }

    public void setIndexerConfiguration(IndexerConfiguration indexerConfiguration)
    {
        this.indexerConfiguration = indexerConfiguration;
    }

    public Indexer getIndexer()
    {
        return indexerConfiguration.getIndexer();
    }

    public Scanner getScanner()
    {
        return indexerConfiguration.getScanner();
    }

    public Map<String, IndexCreator> getIndexers()
    {
        return indexerConfiguration.getIndexers();
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

}
