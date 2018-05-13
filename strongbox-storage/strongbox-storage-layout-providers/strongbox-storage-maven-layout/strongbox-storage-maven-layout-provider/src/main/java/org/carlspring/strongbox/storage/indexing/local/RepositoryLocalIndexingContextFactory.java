package org.carlspring.strongbox.storage.indexing.local;

import org.carlspring.strongbox.storage.indexing.AbstractRepositoryIndexingContextFactory;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexDirectoryPathResolver;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexDirectoryPathResolver.RepositoryIndexDirectoryPathResolverQualifier;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexingContextFactory.RepositoryIndexingContextFactoryQualifier;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
@RepositoryIndexingContextFactoryQualifier(IndexTypeEnum.LOCAL)
public class RepositoryLocalIndexingContextFactory
        extends AbstractRepositoryIndexingContextFactory
{

    @Inject
    @RepositoryIndexDirectoryPathResolverQualifier(IndexTypeEnum.LOCAL)
    private RepositoryIndexDirectoryPathResolver indexDirectoryPathResolver;

    @Override
    protected RepositoryIndexDirectoryPathResolver getRepositoryIndexDirectoryPathResolver()
    {
        return indexDirectoryPathResolver;
    }
}
