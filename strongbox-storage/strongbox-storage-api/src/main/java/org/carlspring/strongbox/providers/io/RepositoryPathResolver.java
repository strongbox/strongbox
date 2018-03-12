package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.util.Objects;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class RepositoryPathResolver
{

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    public RepositoryPath resolve(final Repository repository,
                                  final String... paths)
    {
        Objects.requireNonNull(repository, "Repository should be provided");

        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        RepositoryPath repositoryPath = layoutProvider.resolve(repository);
        if (paths != null)
        {
            for (final String path : paths)
            {
                repositoryPath = repositoryPath.resolve(path);
            }
        }
        return repositoryPath;
    }


}
