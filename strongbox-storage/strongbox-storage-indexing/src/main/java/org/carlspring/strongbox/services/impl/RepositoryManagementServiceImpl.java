package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.ReindexArtifactScanningListener;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexerFactory;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("repositoryManagementService")
public class RepositoryManagementServiceImpl
        implements RepositoryManagementService
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryManagementServiceImpl.class);

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;

    @Autowired
    private RepositoryIndexerFactory repositoryIndexerFactory;

    @Autowired
    private ConfigurationManager configurationManager;

    @Inject
    private IndexPacker indexPacker;


    @Override
    public void createRepository(String storageId,
                                 String repositoryId)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);

        final String storageBasedirPath = storage.getBasedir();
        final File repositoryBasedir = new File(storageBasedirPath, repositoryId).getAbsoluteFile();

        createRepositoryStructure(storageBasedirPath, repositoryId);

        final File indexDir = new File(repositoryBasedir, ".index");

        RepositoryIndexer repositoryIndexer = repositoryIndexerFactory.createRepositoryIndexer(storageId,
                                                                                               repositoryId,
                                                                                               repositoryBasedir,
                                                                                               indexDir);

        repositoryIndexManager.addRepositoryIndex(storageId + ":" + repositoryId, repositoryIndexer);
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

        RepositoryIndexer repositoryIndexer = repositoryIndexManager.getRepositoryIndex(storageId + ":" + repositoryId);

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
            final RepositoryIndexer sourceIndex = repositoryIndexManager.getRepositoryIndex(sourceStorage + ":" +
                                                                                            sourceRepositoryId);
            if (sourceIndex == null)
            {
                throw new ArtifactStorageException("Source repository not found!");
            }

            final RepositoryIndexer targetIndex = repositoryIndexManager.getRepositoryIndex(targetStorage + ":" + targetRepositoryId);
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
        logger.info("Packing index for " + storageId + ":" + repositoryId + "...");

        final RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndex(storageId + ":" + repositoryId);

        IndexingContext context = indexer.getIndexingContext();
        final IndexSearcher indexSearcher = context.acquireIndexSearcher();
        try
        {
            IndexPackingRequest request = new IndexPackingRequest(context, indexSearcher.getIndexReader(),
                                                                  new File(indexer.getRepositoryBasedir() + "/.index"));
            request.setUseTargetProperties(true);
            indexPacker.packIndex(request);

            logger.info("Index for " + storageId + ":" + repositoryId + " was packed successfully.");
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
