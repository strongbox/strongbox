package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.ReindexArtifactScanningListener;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.downloader.IndexDownloadRequest;
import org.carlspring.strongbox.storage.indexing.downloader.IndexDownloader;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

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
import static org.carlspring.strongbox.util.IndexContextHelper.getContextId;

/**
 * @author carlspring
 */
@Component
public class MavenRepositoryFeatures implements RepositoryFeatures
{

    private static final Logger logger = LoggerFactory.getLogger(MavenRepositoryFeatures.class);

    @Inject
    private IndexDownloader downloader;

    @Inject
    private IndexPacker indexPacker;

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private MavenRepositoryManagementStrategy mavenRepositoryManagementStrategy;


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
        RepositoryIndexer repositoryIndexer = mavenRepositoryManagementStrategy.createRepositoryIndexer(storageId,
                                                                                                        repositoryId,
                                                                                                        IndexTypeEnum.REMOTE.getType(),
                                                                                                        repositoryBasedir);

        IndexDownloadRequest request = new IndexDownloadRequest();
        request.setIndexingContextId(storageId + ":" + repositoryId + ":" + IndexTypeEnum.REMOTE.getType());
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

    public int reIndex(String storageId,
                       String repositoryId,
                       String path)
            throws IOException
    {
        String contextId = getContextId(storageId, repositoryId, IndexTypeEnum.LOCAL.getType());

        logger.info("Re-indexing " + contextId + (path != null ? ":" + path : "") + "...");

        RepositoryIndexer repositoryIndexer = repositoryIndexManager.getRepositoryIndexer(contextId);

        File startingPath = path != null ? new File(path) : new File(".");

        IndexingContext context = repositoryIndexer.getIndexingContext();

        ScanningRequest scanningRequest = new ScanningRequest(context,
                                                              new ReindexArtifactScanningListener(repositoryIndexer.getIndexer()),
                                                              startingPath.getPath());

        ScanningResult scan = repositoryIndexer.getScanner().scan(scanningRequest);

        return scan.getTotalFiles();
    }

    public void mergeIndexes(String sourceStorageId,
                             String sourceRepositoryId,
                             String targetStorageId,
                             String targetRepositoryId)
            throws ArtifactStorageException
    {
        try
        {
            String sourceContextId = getContextId(sourceStorageId, sourceRepositoryId, IndexTypeEnum.LOCAL.getType());
            final RepositoryIndexer sourceIndex = repositoryIndexManager.getRepositoryIndexer(sourceContextId);
            if (sourceIndex == null)
            {
                throw new ArtifactStorageException("Source repository not found!");
            }

            String targetContextId = getContextId(targetStorageId, targetRepositoryId, IndexTypeEnum.LOCAL.getType());
            final RepositoryIndexer targetIndex = repositoryIndexManager.getRepositoryIndexer(targetContextId);
            if (targetIndex == null)
            {
                throw new ArtifactStorageException("Target repository not found!");
            }

            targetIndex.getIndexingContext().merge(FSDirectory.open(sourceIndex.getIndexDir().toPath()));
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    public void pack(String storageId,
                     String repositoryId)
            throws IOException
    {
        String contextId = getContextId(storageId, repositoryId, IndexTypeEnum.LOCAL.getType());

        logger.info("Packing index for " + contextId + " ...");

        final RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndexer(contextId);
        if (indexer == null)
        {
            throw new NullPointerException("Unable to find RepositoryIndexer by key " + contextId +
                                           ". \nAvailable keys are " + repositoryIndexManager.getIndexes().keySet());
        }

        IndexingContext context = indexer.getIndexingContext();
        final IndexSearcher indexSearcher = context.acquireIndexSearcher();
        try
        {
            IndexPackingRequest request = new IndexPackingRequest(context,
                                                                  indexSearcher.getIndexReader(),
                                                                  new File(indexer.getRepositoryBasedir() +
                                                                           "/.index/local"));
            request.setUseTargetProperties(true);
            indexPacker.packIndex(request);

            logger.info("Index for " + storageId + ":" + repositoryId + ":" + IndexTypeEnum.LOCAL.getType() +
                        " was packed successfully.");
        }
        finally
        {
            context.releaseIndexSearcher(indexSearcher);
        }
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
