package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.RepositoryInitializationException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.ReindexArtifactScanningListener;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexerFactory;
import org.carlspring.strongbox.storage.indexing.downloader.IndexDownloadRequest;
import org.carlspring.strongbox.storage.indexing.downloader.IndexDownloader;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.maven.index.ScanningRequest;
import org.apache.maven.index.ScanningResult;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.packer.IndexPacker;
import org.apache.maven.index.packer.IndexPackingRequest;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("repositoryManagementService")
public class RepositoryManagementServiceImpl
        implements RepositoryManagementService
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryManagementServiceImpl.class);

    @Inject
    private IndexDownloader downloader;

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    @Inject
    private RepositoryIndexerFactory repositoryIndexerFactory;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private IndexPacker indexPacker;


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

    private RepositoryIndexer createRepositoryIndexer(String storageId,
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

        String contextId = storageId + ":" + repositoryId + ":" + indexType;

        RepositoryIndexer repositoryIndexer = repositoryIndexerFactory.createRepositoryIndexer(contextId,
                                                                                               repositoryId,
                                                                                               indexType,
                                                                                               repositoryBasedir,
                                                                                               repositoryIndexDir);

        repositoryIndexManager.addRepositoryIndexer(contextId, repositoryIndexer);

        return repositoryIndexer;
    }

    @Override
    public void downloadRemoteIndex(String storageId,
                                    String repositoryId)
            throws ArtifactTransportException, RepositoryInitializationException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        File repositoryBasedir = new File(repository.getBasedir());

        File remoteIndexDirectory = new File(repositoryBasedir, ".index/remote");
        if (!remoteIndexDirectory.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            remoteIndexDirectory.mkdirs();
        }

        // Create a remote index
        RepositoryIndexer repositoryIndexer = createRepositoryIndexer(storageId,
                                                                      repositoryId,
                                                                      IndexTypeEnum.REMOTE.getType(),
                                                                      repositoryBasedir);


        IndexDownloadRequest request = new IndexDownloadRequest();
        request.setIndexingContextId(storageId + ":" + repositoryId + ":remote");
        request.setStorageId(storageId);
        request.setRepositoryId(repositoryId);
        request.setRemoteRepositoryURL(repository.getRemoteRepository().getUrl());
        request.setIndexLocalCacheDir(repositoryBasedir);
        request.setIndexDir(remoteIndexDirectory.toString());
        request.setIndexer(repositoryIndexer.getIndexer());

        try
        {
            downloader.download(request);
        }
        catch (IOException | ComponentLookupException e)
        {
            throw new ArtifactTransportException("Failed to retrieve remote index for " +
                                                 storageId + ":" + repositoryId + "!");
        }
    }

    private void createRepositoryStructure(String storageBasedirPath,
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
    public int reIndex(String storageId,
                       String repositoryId,
                       String path)
            throws IOException
    {
        logger.info("Re-indexing " + storageId + ":" + repositoryId + (path != null ? ":" + path : "") + "...");

        RepositoryIndexer repositoryIndexer = repositoryIndexManager.getRepositoryIndexer(storageId + ":" + repositoryId + ":local");

        File startingPath = path != null ? new File(path) : new File(".");

        IndexingContext context = repositoryIndexer.getIndexingContext();

        ScanningRequest scanningRequest = new ScanningRequest(context,
                                                              new ReindexArtifactScanningListener(repositoryIndexer.getIndexer()),
                                                              startingPath.getPath());

        ScanningResult scan = repositoryIndexer.getScanner().scan(scanningRequest);

        return scan.getTotalFiles();
    }

    @Override
    public void mergeIndexes(String sourceStorage,
                             String sourceRepositoryId,
                             String targetStorage,
                             String targetRepositoryId)
            throws ArtifactStorageException
    {
        try
        {
            final RepositoryIndexer sourceIndex = repositoryIndexManager.getRepositoryIndexer(sourceStorage + ":" +
                                                                                              sourceRepositoryId);
            if (sourceIndex == null)
            {
                throw new ArtifactStorageException("Source repository not found!");
            }

            final RepositoryIndexer targetIndex = repositoryIndexManager.getRepositoryIndexer(targetStorage + ":" + targetRepositoryId);
            if (targetIndex == null)
            {
                throw new ArtifactStorageException("Target repository not found!");
            }

            targetIndex.getIndexingContext().merge(FSDirectory.open(sourceIndex.getIndexDir()));
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public void pack(String storageId,
                     String repositoryId)
            throws IOException
    {
        logger.info("Packing index for " + storageId + ":" + repositoryId + ":local...");

        final RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndexer(storageId + ":" + repositoryId + ":local");

        IndexingContext context = indexer.getIndexingContext();
        final IndexSearcher indexSearcher = context.acquireIndexSearcher();
        try
        {
            IndexPackingRequest request = new IndexPackingRequest(context,
                                                                  indexSearcher.getIndexReader(),
                                                                  new File(indexer.getRepositoryBasedir() + "/.index/local"));
            request.setUseTargetProperties(true);
            indexPacker.packIndex(request);

            logger.info("Index for " + storageId + ":" + repositoryId + ":local was packed successfully.");
        }
        finally
        {
            context.releaseIndexSearcher(indexSearcher);
        }
    }

    @Override
    public void removeRepository(String storageId,
                                 String repositoryId)
            throws IOException
    {
        removeDirectoryStructure(storageId, repositoryId);
    }

    private void removeDirectoryStructure(String storageId,
                                          String repositoryId)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);

        final String storageBasedirPath = storage.getBasedir();

        final File repositoryBaseDir = new File(new File(storageBasedirPath), repositoryId);

        if (repositoryBaseDir.exists())
        {
            FileUtils.deleteDirectory(repositoryBaseDir);

            logger.debug("Removed directory structure for repository '" +
                         repositoryBaseDir.getAbsolutePath() + File.separatorChar + repositoryId + "'.");
        }
        else
        {
            throw new IOException("Failed to delete non-existing repository " + repositoryBaseDir.getAbsolutePath() + ".");
        }
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
