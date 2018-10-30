package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.MavenIndexerEnabledCondition;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.ReindexArtifactScanningListener;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.downloader.IndexDownloadRequest;
import org.carlspring.strongbox.storage.indexing.downloader.IndexDownloader;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.xml.configuration.repository.MavenRepositoryConfiguration;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.util.IndexContextHelper.getContextId;

/**
 * @author Przemyslaw Fusik
 */
@Component
@Conditional(MavenIndexerEnabledCondition.class)
public class IndexedMavenRepositoryFeatures
        extends MavenRepositoryFeatures
{

    private static final Logger logger = LoggerFactory.getLogger(IndexedMavenRepositoryFeatures.class);

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Inject
    private IndexDownloader indexDownloader;

    @Inject
    private IndexPacker indexPacker;

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    @Inject
    private IndexedMavenRepositoryManagementStrategy mavenRepositoryManagementStrategy;

    public void downloadRemoteIndex(String storageId,
                                    String repositoryId)
            throws ArtifactTransportException,
                   IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        RepositoryPath repositoryBasedir = repositoryPathResolver.resolve(repository);
        RepositoryPath remoteIndexDirectory = repositoryBasedir.resolve(".index").resolve(
                IndexTypeEnum.REMOTE.getType());

        if (!Files.exists(remoteIndexDirectory))
        {
            //noinspection ResultOfMethodCallIgnored
            Files.createDirectories(remoteIndexDirectory);
        }

        // Create a remote index
        RepositoryIndexer repositoryIndexer;
        String contextId = storageId + ":" + repositoryId + ":" + IndexTypeEnum.REMOTE.getType();
        if (repositoryIndexManager.getRepositoryIndexer(contextId) == null)
        {
            repositoryIndexer = mavenRepositoryManagementStrategy.createRepositoryIndexer(storageId,
                                                                                          repositoryId,
                                                                                          IndexTypeEnum.REMOTE.getType(),
                                                                                          repositoryBasedir);
        }
        else
        {
            repositoryIndexer = repositoryIndexManager.getRepositoryIndexer(contextId);
        }

        IndexDownloadRequest request = new IndexDownloadRequest(repository, repositoryIndexer.getIndexer());

        try
        {
            indexDownloader.download(request);
        }
        catch (IOException | ComponentLookupException e)
        {
            throw new ArtifactTransportException("Failed to retrieve remote index for " +
                                                 storageId + ":" + repositoryId + "!", e);
        }
    }

    public int reIndex(String storageId,
                       String repositoryId,
                       String path)
    {
        String contextId = getContextId(storageId, repositoryId, IndexTypeEnum.LOCAL.getType());

        logger.info("Re-indexing " + contextId + (path != null ? ":" + path : "") + "...");

        RepositoryIndexer repositoryIndexer = repositoryIndexManager.getRepositoryIndexer(contextId);


        IndexingContext context = repositoryIndexer.getIndexingContext();

        ScanningRequest scanningRequest = new ScanningRequest(context,
                                                              new ReindexArtifactScanningListener(repositoryIndexer.getIndexer()),
                                                              path != null ? path : ".");

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

            targetIndex.getIndexingContext().merge(FSDirectory.open(sourceIndex.getIndexDir()));
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    public Path pack(String storageId,
                     String repositoryId)
            throws IOException
    {
        RepositoryIndexer indexer = getIndexer(storageId, repositoryId);
        IndexingContext context = indexer.getIndexingContext();
        Path indexPath = resolveIndexPath(storageId, repositoryId, null);
        final IndexSearcher indexSearcher = context.acquireIndexSearcher();
        try
        {

            IndexPackingRequest request = new IndexPackingRequest(context,
                                                                  indexSearcher.getIndexReader(),
                                                                  indexPath.toFile());
            request.setUseTargetProperties(true);
            indexPacker.packIndex(request);

            logger.info("Index for " + storageId + ":" + repositoryId + ":" + IndexTypeEnum.LOCAL.getType() +
                        " was packed successfully.");
        }
        finally
        {
            context.releaseIndexSearcher(indexSearcher);
        }
        return indexPath.resolve(IndexingContext.INDEX_FILE_PREFIX + ".gz");
    }

    public Path resolveIndexPath(String storageId,
                                 String repositoryId,
                                 String path)
            throws RepositoryIndexerNotFoundException
    {
        final RepositoryIndexer indexer = getIndexer(storageId, repositoryId);
        final Path result = indexer.getIndexDir();
        return path == null ?
               result :
               result.resolve(path);
    }

    public boolean isIndexingEnabled(Repository repository)
    {
        MavenRepositoryConfiguration repositoryConfiguration = (MavenRepositoryConfiguration) repository.getRepositoryConfiguration();

        return repositoryConfiguration != null && repositoryConfiguration.isIndexingEnabled();
    }

    private RepositoryIndexer getIndexer(String storageId,
                                         String repositoryId)
            throws RepositoryIndexerNotFoundException
    {
        String contextId = getContextId(storageId, repositoryId, IndexTypeEnum.LOCAL.getType());

        final RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndexer(contextId);
        if (indexer == null)
        {
            throw new RepositoryIndexerNotFoundException("Unable to find a repository indexer '" + contextId + "'.\n" +
                                                         "The available contextId-s are " +
                                                         repositoryIndexManager.getIndexes().keySet());
        }

        return indexer;
    }

}
