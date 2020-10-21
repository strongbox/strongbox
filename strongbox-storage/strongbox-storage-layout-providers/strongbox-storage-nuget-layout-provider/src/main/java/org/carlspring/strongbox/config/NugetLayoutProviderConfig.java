package org.carlspring.strongbox.config;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.providers.storage.StorageProvider;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.providers.io.LayoutFileSystemFactory;
import org.carlspring.strongbox.providers.io.LayoutFileSystemProviderFactory;
import org.carlspring.strongbox.providers.layout.LayoutFileSystemProvider;
import org.carlspring.strongbox.providers.layout.NugetFileSystem;
import org.carlspring.strongbox.providers.layout.NugetFileSystemProvider;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.nio.file.FileSystem;
import java.nio.file.spi.FileSystemProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.configuration",
                 "org.carlspring.strongbox.repository",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage" })
public class NugetLayoutProviderConfig
{

    public static final String FILE_SYSTEM_ALIAS = "LayoutFileSystemFactory." + NugetLayoutProvider.ALIAS;
    public static final String FILE_SYSTEM_PROVIDER_ALIAS = "LayoutFileSystemProviderFactory."
            + NugetLayoutProvider.ALIAS;

    @Inject
    protected StorageProviderRegistry storageProviderRegistry;

    @Bean(FILE_SYSTEM_PROVIDER_ALIAS)
    public LayoutFileSystemProviderFactory nugetRepositoryFileSystemProviderFactory()
    {
        return (repository) -> {
            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getStorageProvider());

            LayoutFileSystemProvider result = nugetFileSystemProvider(storageProvider.getFileSystemProvider());

            return result;
        };

    }

    @Bean
    @Scope("prototype")
    public NugetFileSystemProvider nugetFileSystemProvider(FileSystemProvider provider)
    {
        return new NugetFileSystemProvider(provider);
    }

    @Bean(FILE_SYSTEM_ALIAS)
    public LayoutFileSystemFactory nugetRepositoryFileSystemFactory(PropertiesBooter propertiesBooter)
    {
        return (repository) -> {
            LayoutFileSystemProviderFactory providerFactory = nugetRepositoryFileSystemProviderFactory();

            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getStorageProvider());

            return nugetRepositoryFileSystem(propertiesBooter, repository, storageProvider.getFileSystem(),
                                             providerFactory.create(repository));
        };
    }

    @Bean
    @Scope("prototype")
    public NugetFileSystem nugetRepositoryFileSystem(PropertiesBooter propertiesBooter,
                                                     Repository repository,
                                                     FileSystem storageFileSystem,
                                                     LayoutFileSystemProvider provider)
    {
        return new NugetFileSystem(propertiesBooter, repository, storageFileSystem, provider);
    }

}
