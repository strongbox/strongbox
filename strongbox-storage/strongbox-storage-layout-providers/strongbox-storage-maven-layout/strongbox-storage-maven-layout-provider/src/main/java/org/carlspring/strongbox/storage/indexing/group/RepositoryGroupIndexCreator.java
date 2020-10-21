package org.carlspring.strongbox.storage.indexing.group;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.configuration.ConfigurationUtils;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.*;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexDirectoryPathResolver.RepositoryIndexDirectoryPathResolverQualifier;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexCreator.RepositoryIndexCreatorQualifier;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexingContextFactory.RepositoryIndexingContextFactoryQualifier;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.locks.Lock;

import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
@RepositoryIndexCreatorQualifier(RepositoryTypeEnum.GROUP)
public class RepositoryGroupIndexCreator
        extends AbstractRepositoryIndexCreator
{

    @Inject
    @RepositoryIndexDirectoryPathResolverQualifier(IndexTypeEnum.LOCAL)
    private RepositoryIndexDirectoryPathResolver localIndexDirectoryPathResolver;

    @Inject
    @RepositoryIndexDirectoryPathResolverQualifier(IndexTypeEnum.REMOTE)
    private RepositoryIndexDirectoryPathResolver remoteIndexDirectoryPathResolver;

    @Inject
    @RepositoryIndexingContextFactoryQualifier(IndexTypeEnum.LOCAL)
    private RepositoryIndexingContextFactory indexingContextFactory;

    @Inject
    private ConfigurationManager configurationManager;

    @Override
    protected void onIndexingContextCreated(final RepositoryPath repositoryIndexDirectoryPath,
                                            final RepositoryCloseableIndexingContext indexingContext)
            throws IOException
    {

        indexingContext.purge();
        mergeSubrepositoryIndexes(indexingContext);
        IndexPacker.pack(repositoryIndexDirectoryPath, indexingContext);
    }

    private void mergeSubrepositoryIndexes(RepositoryCloseableIndexingContext indexingContext)
            throws IOException
    {
        final Repository repository = indexingContext.getRepositoryRaw();
        final Storage storage = repository.getStorage();

        for (final String storageAndRepositoryId : repository.getGroupRepositories())
        {
            final String sId = ConfigurationUtils.getStorageId(storage.getId(), storageAndRepositoryId);
            final String rId = ConfigurationUtils.getRepositoryId(storageAndRepositoryId);

            final RepositoryPath subRepositoryIndexDirectoryPath = getSubRepositoryIndexPath(sId, rId);

            final Lock lock = repositoryPathLock.lock(subRepositoryIndexDirectoryPath).readLock();
            lock.lock();
            try
            {
                try
                {
                    indexingContext.merge(new SimpleFSDirectory(subRepositoryIndexDirectoryPath));
                }
                catch (IndexNotFoundException ex)
                {
                    logger.warn("IndexNotFound in [{}]", subRepositoryIndexDirectoryPath, ex);
                }
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    private RepositoryPath getSubRepositoryIndexPath(final String storageId,
                                                     final String repositoryId)
    {
        final Repository repository = configurationManager.getRepository(storageId, repositoryId);

        final RepositoryIndexDirectoryPathResolver indexDirectoryPathResolver =
                repository.isProxyRepository() ? remoteIndexDirectoryPathResolver :
                localIndexDirectoryPathResolver;

        return indexDirectoryPathResolver.resolve(repository);
    }

    @Override
    protected RepositoryIndexingContextFactory getRepositoryIndexingContextFactory()
    {
        return indexingContextFactory;
    }

    @Override
    protected RepositoryIndexDirectoryPathResolver getRepositoryIndexDirectoryPathResolver()
    {
        return localIndexDirectoryPathResolver;
    }
}

