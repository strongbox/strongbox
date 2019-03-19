package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.config.MavenIndexerEnabledCondition;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repository.RepositoryInitializationException;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.xml.configuration.repository.MavenRepositoryConfiguration;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.creator.JarFileContentsIndexCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("repositoryIndexerFactory")
@Conditional(MavenIndexerEnabledCondition.class)
public class RepositoryIndexerFactory
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryIndexerFactory.class);

    @Inject
    private IndexerConfiguration indexerConfiguration;

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private ConfigurationManager configurationManager;

    public RepositoryIndexer createRepositoryIndexer(String storageId,
                                                     String repositoryId,
                                                     String indexType,
                                                     RepositoryPath repositoryBasedir,
                                                     RepositoryPath indexDir)
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
        repositoryIndexer.setIndexDir(indexDir);
        repositoryIndexer.setIndexingContext(indexingContext);
        repositoryIndexer.setIndexer(indexerConfiguration.getIndexer());
        repositoryIndexer.setScanner(indexerConfiguration.getScanner());
        repositoryIndexer.setConfiguration(configurationManager.getConfiguration());
        repositoryIndexer.setApplicationContext(applicationContext);

        return repositoryIndexer;
    }

    private IndexingContext createIndexingContext(String storageId,
                                                  String repositoryId,
                                                  String indexType,
                                                  RepositoryPath repositoryBasedir,
                                                  RepositoryPath indexDir)
            throws IOException
    {
        return indexerConfiguration.getIndexer()
                                   .createIndexingContext(storageId + ":" + repositoryId + ":" + indexType,
                                                          repositoryId,
                                                          repositoryBasedir.toFile(),
                                                          indexDir.toFile(),
                                                          null,
                                                          null,
                                                          true, // if context should be searched in non-targeted mode.
                                                          true, // if indexDirectory is known to contain (or should contain)
                                                                // valid Maven Indexer lucene index, and no checks needed to be
                                                                // performed, or, if we want to "stomp" over existing index
                                                                // (unsafe to do!).
                                                          getRepositoryIndexCreators(storageId, repositoryId));
    }

    private List<IndexCreator> getRepositoryIndexCreators(final String storageId,
                                                          final String repositoryId)
    {
        List<IndexCreator> indexCreators = Lists.newArrayList(indexerConfiguration.getIndexersAsList());
        ImmutableRepository repository = (ImmutableRepository) configurationManager.getConfiguration()
                                                                                   .getStorage(storageId)
                                                                                   .getRepository(repositoryId);
        final MavenRepositoryConfiguration mavenRepositoryConfiguration = (MavenRepositoryConfiguration) repository.getRepositoryConfiguration();
        if (mavenRepositoryConfiguration != null && !mavenRepositoryConfiguration.isIndexingClassNamesEnabled())
        {
            indexCreators = indexCreators.stream()
                                         .filter(indexCreator -> !(indexCreator instanceof JarFileContentsIndexCreator))
                                         .collect(Collectors.toList());
        }
        return indexCreators;
    }

}
