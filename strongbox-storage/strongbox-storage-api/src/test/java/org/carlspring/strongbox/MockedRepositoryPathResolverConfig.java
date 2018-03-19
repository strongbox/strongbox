package org.carlspring.strongbox;

import org.carlspring.strongbox.providers.io.RepositoryFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.layout.RepositoryLayoutFileSystemProvider;
import org.carlspring.strongbox.storage.repository.Repository;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
public class MockedRepositoryPathResolverConfig
{
    @Bean
    @Primary
    RepositoryPathResolver repositoryPathResolver(LayoutProviderRegistry layoutProviderRegistry)
    {
        return new RepositoryPathResolver()
        {

            @Override
            public RepositoryPath resolve(final Repository repository,
                                          final String... paths)
            {
                Objects.requireNonNull(repository, "Repository should be provided");

                final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
                if (layoutProvider == null)
                {
                    FileSystem fileSystem = FileSystems.getDefault();
                    RepositoryFileSystem repositoryFileSystem = new RepositoryFileSystem(repository,
                                                                                         fileSystem,
                                                                                         new RepositoryLayoutFileSystemProvider(fileSystem.provider(),
                                                                                                                                null,
                                                                                                                                null))
                    {
                        @Override
                        public Set<String> getDigestAlgorithmSet()
                        {
                            throw new UnsupportedOperationException();
                        }
                    };

                    return new RepositoryPath(Paths.get(repository.getBasedir(), paths), repositoryFileSystem);
                }
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
        };
    }
}
