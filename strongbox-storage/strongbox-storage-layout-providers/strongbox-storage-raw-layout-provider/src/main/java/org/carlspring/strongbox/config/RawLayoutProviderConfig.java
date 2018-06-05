package org.carlspring.strongbox.config;

import java.nio.file.FileSystem;
import java.nio.file.spi.FileSystemProvider;

import javax.inject.Inject;

import org.carlspring.strongbox.providers.datastore.StorageProvider;
import org.carlspring.strongbox.providers.datastore.StorageProviderRegistry;
import org.carlspring.strongbox.providers.io.RepositoryFileSystemFactory;
import org.carlspring.strongbox.providers.io.RepositoryFileSystemProviderFactory;
import org.carlspring.strongbox.providers.layout.RawFileSystem;
import org.carlspring.strongbox.providers.layout.RawFileSystemProvider;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.providers.layout.RepositoryLayoutFileSystemProvider;
import org.carlspring.strongbox.repository.RawRepositoryFeatures;
import org.carlspring.strongbox.repository.RawRepositoryManagementStrategy;
import org.carlspring.strongbox.storage.repository.Repository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.repository",
                 "org.carlspring.strongbox.event",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
})
public class RawLayoutProviderConfig
{
    public static final String FILE_SYSTEM_ALIAS = "RepositoryFileSystemFactory." + RawLayoutProvider.ALIAS;
    public static final String FILE_SYSTEM_PROVIDER_ALIAS = "RepositoryFileSystemProviderFactory."
            + RawLayoutProvider.ALIAS;

    @Inject
    protected StorageProviderRegistry storageProviderRegistry;

    @Bean(name = "rawLayoutProvider")
    RawLayoutProvider rawLayoutProvider()
    {
        return new RawLayoutProvider();
    }

    @Bean(name = "rawRepositoryFeatures")
    RawRepositoryFeatures rawRepositoryFeatures()
    {
        return new RawRepositoryFeatures();
    }

    @Bean(name = "rawRepositoryManagementStrategy")
    RawRepositoryManagementStrategy rawRepositoryManagementStrategy()
    {
        return new RawRepositoryManagementStrategy();
    }

    @Bean(FILE_SYSTEM_PROVIDER_ALIAS)
    public RepositoryFileSystemProviderFactory rawRepositoryFileSystemProviderFactory()
    {
        return (repository) -> {
            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

            RepositoryLayoutFileSystemProvider result = rawFileSystemProvider(storageProvider.getFileSystemProvider());

            return result;
        };

    }

    @Bean
    @Scope("prototype")
    public RawFileSystemProvider rawFileSystemProvider(FileSystemProvider provider)
    {
        return new RawFileSystemProvider(provider);
    }

    @Bean(FILE_SYSTEM_ALIAS)
    public RepositoryFileSystemFactory rawRepositoryFileSystemFactory()
    {
        RepositoryFileSystemProviderFactory providerFactory = rawRepositoryFileSystemProviderFactory();
        
        return (repository) -> {
            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

            return rawRepositoryFileSystem(repository, storageProvider.getFileSystem(),
                                           providerFactory.create(repository));
        };
    }

    @Bean
    @Scope("prototype")
    public RawFileSystem rawRepositoryFileSystem(Repository repository,
                                                 FileSystem storageFileSystem,
                                                 RepositoryLayoutFileSystemProvider provider)
    {
        return new RawFileSystem(repository, storageFileSystem, provider);
    }

}
