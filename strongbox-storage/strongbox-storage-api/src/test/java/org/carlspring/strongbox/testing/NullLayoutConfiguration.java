package org.carlspring.strongbox.testing;

import java.nio.file.FileSystem;
import java.nio.file.spi.FileSystemProvider;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.NullArtifactCoordinates;
import org.carlspring.strongbox.providers.datastore.StorageProvider;
import org.carlspring.strongbox.providers.datastore.StorageProviderRegistry;
import org.carlspring.strongbox.providers.io.LayoutFileSystemFactory;
import org.carlspring.strongbox.providers.io.LayoutFileSystemProviderFactory;
import org.carlspring.strongbox.providers.layout.LayoutFileSystemProvider;
import org.carlspring.strongbox.storage.repository.Repository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class NullLayoutConfiguration
{

    public static final String FILE_SYSTEM_ALIAS = "LayoutFileSystemFactory." + NullArtifactCoordinates.LAYOUT_NAME;
    public static final String FILE_SYSTEM_PROVIDER_ALIAS = "LayoutFileSystemProviderFactory."
            + NullArtifactCoordinates.LAYOUT_NAME;

    @Inject
    private StorageProviderRegistry storageProviderRegistry;

    @Bean
    public NullRepositoryManagementStrategy nullRepositoryManagementStrategy()
    {
        return new NullRepositoryManagementStrategy();
    }

    @Bean
    public NullLayoutProvider nullLayoutProvider()
    {
        return new NullLayoutProvider();
    }

    @Bean(FILE_SYSTEM_PROVIDER_ALIAS)
    public LayoutFileSystemProviderFactory nullRepositoryFileSystemProviderFactory()
    {
        return (repository) -> {
            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

            return nullFileSystemProvider(storageProvider.getFileSystemProvider());
        };

    }

    @Bean
    @Scope("prototype")
    public NullFileSystemProvider nullFileSystemProvider(FileSystemProvider provider)
    {
        return new NullFileSystemProvider(provider);
    }

    @Bean(FILE_SYSTEM_ALIAS)
    public LayoutFileSystemFactory nullRepositoryFileSystemFactory()
    {
        LayoutFileSystemProviderFactory providerFactory = nullRepositoryFileSystemProviderFactory();

        return (repository) -> {
            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

            return nullRepositoryFileSystem(repository, storageProvider.getFileSystem(),
                                            providerFactory.create(repository));
        };
    }

    @Bean
    @Scope("prototype")
    public NullFileSystem nullRepositoryFileSystem(Repository repository,
                                                   FileSystem storageFileSystem,
                                                   LayoutFileSystemProvider provider)
    {
        return new NullFileSystem(repository, storageFileSystem, provider);
    }

}
