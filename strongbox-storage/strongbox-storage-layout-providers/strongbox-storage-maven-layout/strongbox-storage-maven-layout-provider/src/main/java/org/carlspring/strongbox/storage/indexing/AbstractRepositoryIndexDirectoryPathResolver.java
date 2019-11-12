package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;

/**
 * @author Przemyslaw Fusik
 */
public abstract class AbstractRepositoryIndexDirectoryPathResolver
        implements RepositoryIndexDirectoryPathResolver
{

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Override
    public RepositoryPath resolve(Repository repository)
    {
        final RootRepositoryPath rootRepositoryPath = repositoryPathResolver.resolve(repository);
        return rootRepositoryPath.resolve(MavenRepositoryFeatures.INDEX).resolve(getIndexType().getType());
    }

    protected abstract IndexTypeEnum getIndexType();

}
