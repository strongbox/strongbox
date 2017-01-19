package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import org.apache.maven.index.Indexer;
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
    private RepositoryManagementService repositoryManagementService;

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
                                                     File repositoryBasedir,
                                                     File indexDir)
            throws IOException, ArtifactTransportException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        RepositoryIndexer repositoryIndexer = new RepositoryIndexer();
        repositoryIndexer.setStorageId(storageId);
        repositoryIndexer.setRepositoryId(repositoryId);
        repositoryIndexer.setRepositoryBasedir(repositoryBasedir);
        repositoryIndexer.setIndexDir(indexDir);
        if (repository.isProxyRepository())
        {
            repositoryIndexer.setIndexingContext(
                    repositoryManagementService.getRemoteRepositoryIndexingContext(storageId, repositoryId));
        }
        else
        {
            repositoryIndexer.setIndexingContext(createIndexingContext(repositoryId, repositoryBasedir, indexDir));
        }
        repositoryIndexer.setIndexer(indexerConfiguration.getIndexer());
        repositoryIndexer.setScanner(indexerConfiguration.getScanner());
        repositoryIndexer.setConfiguration(configuration);

        return repositoryIndexer;
    }

    private IndexingContext createIndexingContext(String repositoryId,
                                                  File repositoryBasedir,
                                                  File indexDir)
            throws IOException
    {
        return getIndexer().createIndexingContext(repositoryId + "/ctx",
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

    public Indexer getIndexer()
    {
        return indexerConfiguration.getIndexer();
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
