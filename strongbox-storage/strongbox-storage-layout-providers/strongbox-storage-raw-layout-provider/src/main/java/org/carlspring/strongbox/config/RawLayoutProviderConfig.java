package org.carlspring.strongbox.config;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.providers.storage.StorageProvider;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.providers.io.LayoutFileSystemFactory;
import org.carlspring.strongbox.providers.io.LayoutFileSystemProviderFactory;
import org.carlspring.strongbox.providers.layout.LayoutFileSystemProvider;
import org.carlspring.strongbox.providers.layout.RawFileSystem;
import org.carlspring.strongbox.providers.layout.RawFileSystemProvider;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;

import java.nio.file.FileSystem;
import java.nio.file.spi.FileSystemProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.repository",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage" })
public class RawLayoutProviderConfig
{

    public static final String FILE_SYSTEM_ALIAS = "LayoutFileSystemFactory." + RawLayoutProvider.ALIAS;

    public static final String FILE_SYSTEM_PROVIDER_ALIAS = "LayoutFileSystemProviderFactory." +
                                                            RawLayoutProvider.ALIAS;

    @Bean(FILE_SYSTEM_PROVIDER_ALIAS)
    public LayoutFileSystemProviderFactory rawRepositoryFileSystemProviderFactory(StorageProviderRegistry storageProviderRegistry)
    {
        return (repository) -> {
            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getStorageProvider());

            LayoutFileSystemProvider result = rawFileSystemProvider(storageProvider.getFileSystemProvider());

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
    public LayoutFileSystemFactory rawRepositoryFileSystemFactory(PropertiesBooter propertiesBooter,
                                                                  StorageProviderRegistry storageProviderRegistry)
    {
        LayoutFileSystemProviderFactory providerFactory = rawRepositoryFileSystemProviderFactory(storageProviderRegistry);
        
        return (repository) -> {
            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getStorageProvider());

            return rawRepositoryFileSystem(propertiesBooter, repository, storageProvider.getFileSystem(),
                                           providerFactory.create(repository));
        };
    }

    @Bean
    @Scope("prototype")
    public RawFileSystem rawRepositoryFileSystem(PropertiesBooter propertiesBooter,
                                                 Repository repository,
                                                 FileSystem storageFileSystem,
                                                 LayoutFileSystemProvider provider)
    {
        return new RawFileSystem(propertiesBooter, repository, storageFileSystem, provider);
    }

}
