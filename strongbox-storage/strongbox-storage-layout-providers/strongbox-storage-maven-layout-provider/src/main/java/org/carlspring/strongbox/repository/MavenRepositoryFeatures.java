package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.locator.handlers.RemoveTimestampedSnapshotOperation;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.ReindexArtifactScanningListener;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.downloader.IndexDownloadRequest;
import org.carlspring.strongbox.storage.indexing.downloader.IndexDownloader;
import org.carlspring.strongbox.storage.metadata.MavenSnapshotManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.xml.configuration.repository.MavenRepositoryConfiguration;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

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
public class MavenRepositoryFeatures
        implements RepositoryFeatures
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

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private MavenSnapshotManager mavenSnapshotManager;

    @Inject
    public Set<String> defaultArtifactCoordinateValidators;


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
            downloader.download(request);
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
        final Path result = Paths.get(indexer.getRepositoryBasedir()
                                             .getAbsolutePath())
                                 .resolve(".index")
                                 .resolve("local");
        return path == null ?
               result :
               result.resolve(path);
    }

    public void removeTimestampedSnapshots(String storageId,
                                           String repositoryId,
                                           String artifactPath,
                                           int numberToKeep,
                                           int keepPeriod)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        if (repository.getPolicy().equals(RepositoryPolicyEnum.SNAPSHOT.getPolicy()))
        {
            LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
            RepositoryPath repositoryPath = layoutProvider.resolve(repository).resolve(artifactPath);

            RemoveTimestampedSnapshotOperation operation = new RemoveTimestampedSnapshotOperation(mavenSnapshotManager);
            operation.setBasePath(repositoryPath);
            operation.setNumberToKeep(numberToKeep);
            operation.setKeepPeriod(keepPeriod);

            ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
            locator.setOperation(operation);
            locator.locateArtifactDirectories();
        }
        else
        {
            throw new ArtifactStorageException("Type of repository is invalid: repositoryId - " + repositoryId);
        }
    }

    public boolean isIndexingEnabled(Repository repository)
    {
        MavenRepositoryConfiguration repositoryConfiguration = (MavenRepositoryConfiguration) repository.getRepositoryConfiguration();

        return repositoryConfiguration != null && repositoryConfiguration.isIndexingEnabled();
    }

    public boolean isIndexingEnabled(String storageId,
                                     String repositoryId)
    {
        Configuration configuration = configurationManager.getConfiguration();

        Repository repository = configuration.getStorage(storageId).getRepository(repositoryId);

        return isIndexingEnabled(repository);
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
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

    @Override
    public Repository createRepositoryInstance(String storageId, String repositoryId)
    {
        Repository repository = new Repository(repositoryId);
        repository.setStorage(configurationManager.getConfiguration().getStorage(storageId));
        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(getDefaultArtifactCoordinateValidators());

        return repository;
    }

    @Override
    public Set<String> getDefaultArtifactCoordinateValidators()
    {
        return defaultArtifactCoordinateValidators;
    }

}
