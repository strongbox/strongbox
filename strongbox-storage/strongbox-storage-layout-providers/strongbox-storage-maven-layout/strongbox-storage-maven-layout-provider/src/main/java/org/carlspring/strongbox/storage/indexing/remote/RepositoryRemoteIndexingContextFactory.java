package org.carlspring.strongbox.storage.indexing.remote;

import org.carlspring.strongbox.storage.indexing.AbstractRepositoryIndexingContextFactory;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexDirectoryPathResolver;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexDirectoryPathResolver.RepositoryIndexDirectoryPathResolverQualifier;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexingContextFactory.RepositoryIndexingContextFactoryQualifier;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
@RepositoryIndexingContextFactoryQualifier(IndexTypeEnum.REMOTE)
public class RepositoryRemoteIndexingContextFactory
        extends AbstractRepositoryIndexingContextFactory
{

    @Inject
    @RepositoryIndexDirectoryPathResolverQualifier(IndexTypeEnum.REMOTE)
    private RepositoryIndexDirectoryPathResolver indexDirectoryPathResolver;

    @Override
    protected RepositoryIndexDirectoryPathResolver getRepositoryIndexDirectoryPathResolver()
    {
        return indexDirectoryPathResolver;
    }

    @Override
    protected String getRepositoryUrl(final Repository repository)
    {
        final RemoteRepository remoteRepository = repository.getRemoteRepository();
        if (remoteRepository == null)
        {
            logger.warn("Repository [{}:{}] was expected to have remote repository provided but was null.",
                        repository.getStorage().getId(), repository.getId());
            return null;

        }
        return remoteRepository.getUrl();
    }
}
