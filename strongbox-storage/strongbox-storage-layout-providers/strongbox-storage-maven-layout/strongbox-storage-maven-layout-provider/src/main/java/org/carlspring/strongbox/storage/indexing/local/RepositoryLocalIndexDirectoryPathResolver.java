package org.carlspring.strongbox.storage.indexing.local;

import org.carlspring.strongbox.storage.indexing.AbstractRepositoryIndexDirectoryPathResolver;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexDirectoryPathResolver.RepositoryIndexDirectoryPathResolverQualifier;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
@RepositoryIndexDirectoryPathResolverQualifier(IndexTypeEnum.LOCAL)
public class RepositoryLocalIndexDirectoryPathResolver
        extends AbstractRepositoryIndexDirectoryPathResolver
{

    @Override
    protected IndexTypeEnum getIndexType()
    {
        return IndexTypeEnum.LOCAL;
    }
}
