package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexerFactory;
import org.carlspring.strongbox.storage.indexing.downloader.IndexDownloader;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import org.apache.maven.index.packer.IndexPacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.util.IndexContextHelper.getContextId;

/**
 * @author carlspring
 */
@Component
public class MavenRepositoryManagementStrategy extends AbstractRepositoryManagementStrategy
{

    private static final Logger logger = LoggerFactory.getLogger(MavenRepositoryManagementStrategy.class);

    @Inject
    private IndexDownloader downloader;

    @Inject
    private IndexPacker indexPacker;

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    @Inject
    private RepositoryIndexerFactory repositoryIndexerFactory;

    @Inject
    private ConfigurationManager configurationManager;


    @Override
    public void createRepository(String storageId,
                                 String repositoryId)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        final String storageBasedirPath = storage.getBasedir();
        final File repositoryBasedir = new File(storageBasedirPath, repositoryId).getAbsoluteFile();

        createRepositoryStructure(storageBasedirPath, repositoryId);

        if (repository.isIndexingEnabled())
        {
            if (repository.isProxyRepository())
            {
                // Create a remote index
                createRepositoryIndexer(storageId, repositoryId, IndexTypeEnum.REMOTE.getType(), repositoryBasedir);
            }

            // Create a local index
            createRepositoryIndexer(storageId, repositoryId, IndexTypeEnum.LOCAL.getType(), repositoryBasedir);
        }
    }

    public RepositoryIndexer createRepositoryIndexer(String storageId,
                                                     String repositoryId,
                                                     String indexType,
                                                     File repositoryBasedir)
            throws RepositoryInitializationException
    {
        File repositoryIndexDir = new File(repositoryBasedir, ".index/" + indexType);
        if (!repositoryIndexDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            repositoryIndexDir.mkdirs();
        }

        RepositoryIndexer repositoryIndexer = repositoryIndexerFactory.createRepositoryIndexer(storageId,
                                                                                               repositoryId,
                                                                                               indexType,
                                                                                               repositoryBasedir,
                                                                                               repositoryIndexDir);

        String contextId = storageId + ":" + repositoryId + ":" + indexType;

        repositoryIndexManager.addRepositoryIndexer(contextId, repositoryIndexer);

        return repositoryIndexer;
    }

    @Override
    public void createRepositoryStructure(String storageBasedirPath,
                                          String repositoryId)
            throws IOException
    {
        final File storageBasedir = new File(storageBasedirPath);
        final File repositoryDir = new File(storageBasedir, repositoryId);

        if (!repositoryDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            repositoryDir.mkdirs();
            //noinspection ResultOfMethodCallIgnored
            new File(repositoryDir, ".index").mkdirs();
            //noinspection ResultOfMethodCallIgnored
            new File(repositoryDir, ".trash").mkdirs();
        }
    }

    @Override
    public void initializeRepository(String storageId, String repositoryId)
            throws RepositoryInitializationException
    {
        // initializeRepositoryIndexes(storageId, repositoryId);
    }

    public void initializeRepositoryIndexes(String storageId,
                                            String repositoryId)
            throws RepositoryInitializationException
    {
        try
        {
            Storage storage = getConfiguration().getStorage(storageId);

            final File repositoryBasedir = new File(storage.getBasedir(), repositoryId);

            if (storage.getRepository(repositoryId).isIndexingEnabled())
            {
                initializeRepositoryIndex(storage, repositoryId, IndexTypeEnum.LOCAL.getType(), repositoryBasedir);

                if (storage.getRepository(repositoryId).isProxyRepository())
                {
                    initializeRepositoryIndex(storage, repositoryId, IndexTypeEnum.REMOTE.getType(), repositoryBasedir);
                }
            }
        }
        catch (RepositoryInitializationException e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    public void initializeRepositoryIndex(Storage storage,
                                          String repositoryId,
                                          String indexType,
                                          File repositoryBasedir)
            throws RepositoryInitializationException
    {
        final File indexDir = new File(repositoryBasedir, ".index/" + indexType);

        RepositoryIndexer repositoryIndexer = repositoryIndexerFactory.createRepositoryIndexer(storage.getId(),
                                                                                               repositoryId,
                                                                                               indexType,
                                                                                               repositoryBasedir,
                                                                                               indexDir);

        String contextId = getContextId(storage.getId(), repositoryId, IndexTypeEnum.LOCAL.getType());

        repositoryIndexManager.addRepositoryIndexer(contextId, repositoryIndexer);
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
