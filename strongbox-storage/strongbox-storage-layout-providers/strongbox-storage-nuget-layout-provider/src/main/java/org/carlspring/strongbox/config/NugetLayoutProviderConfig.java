package org.carlspring.strongbox.config;

import java.nio.file.FileSystem;
import java.nio.file.spi.FileSystemProvider;

import javax.inject.Inject;

import org.carlspring.strongbox.providers.datastore.StorageProvider;
import org.carlspring.strongbox.providers.datastore.StorageProviderRegistry;
import org.carlspring.strongbox.providers.io.RepositoryFileSystemFactory;
import org.carlspring.strongbox.providers.io.RepositoryFileSystemProviderFactory;
import org.carlspring.strongbox.providers.layout.NugetFileSystemProvider;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.providers.layout.NugetFileSystem;
import org.carlspring.strongbox.providers.layout.RepositoryLayoutFileSystemProvider;
import org.carlspring.strongbox.storage.repository.Repository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.configuration",
                 "org.carlspring.strongbox.event",
                 "org.carlspring.strongbox.repository",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage" })
@Import({ EventsConfig.class })
public class NugetLayoutProviderConfig
{

    public static final String FILE_SYSTEM_ALIAS = "RepositoryFileSystemFactory." + NugetLayoutProvider.ALIAS;
    public static final String FILE_SYSTEM_PROVIDER_ALIAS = "RepositoryFileSystemProviderFactory."
            + NugetLayoutProvider.ALIAS;

    @Inject
    protected StorageProviderRegistry storageProviderRegistry;

    @Bean(FILE_SYSTEM_PROVIDER_ALIAS)
    public RepositoryFileSystemProviderFactory nugetRepositoryFileSystemProviderFactory()
    {
        return (repository) -> {
            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

            RepositoryLayoutFileSystemProvider result = nugetFileSystemProvider(storageProvider.getFileSystemProvider());

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
    public RepositoryFileSystemFactory nugetRepositoryFileSystemFactory()
    {
        return (repository) -> {
            RepositoryFileSystemProviderFactory providerFactory = nugetRepositoryFileSystemProviderFactory();

            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

            return nugetRepositoryFileSystem(repository, storageProvider.getFileSystem(),
                                             providerFactory.create(repository));
        };
    }

    @Bean
    @Scope("prototype")
    public NugetFileSystem nugetRepositoryFileSystem(Repository repository,
                                                               FileSystem storageFileSystem,
                                                               RepositoryLayoutFileSystemProvider provider)
    {
        return new NugetFileSystem(repository, storageFileSystem, provider);
    }

}
