package org.carlspring.strongbox;

import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.io.RepositoryFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.RepositoryLayoutFileSystemProvider;
import org.carlspring.strongbox.storage.repository.Repository;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

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
    RepositoryPathResolver repositoryPathResolver()
    {
        return new TestRepositoryPathResolver();
    }
    
    public class TestRepositoryPathResolver extends RepositoryPathResolver
    {

        @Override
        public RepositoryPath resolve(final Repository repository,
                                      final String path)
        {
//            final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
//            if (layoutProvider != null)
//            {
//                return super.resolve(repository, path);
//            }
//            FileSystem fileSystem = FileSystems.getDefault();
//            RepositoryFileSystem repositoryFileSystem = new RepositoryFileSystem(repository,
//                    fileSystem,
//                    new RepositoryLayoutFileSystemProvider(fileSystem.provider(),
//                            null,
//                            null))
//            {
//                @Override
//                public Set<String> getDigestAlgorithmSet()
//                {
//                    return Collections.emptySet();
//                }
//            };
//
//            RootRepositoryPath result = new RootRepositoryPath(Paths.get(repository.getBasedir()),
//                    repositoryFileSystem);
//
//            ArtifactEntry artifactEntry = new ArtifactEntry();
//            artifactEntry.setUuid(UUID.randomUUID().toString());
//            return result.resolve(artifactEntry);
            return super.resolve(repository, path);
        }
    }
}
