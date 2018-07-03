package org.carlspring.strongbox.config;

import java.nio.file.FileSystem;
import java.nio.file.spi.FileSystemProvider;

import javax.inject.Inject;

import org.carlspring.strongbox.providers.datastore.StorageProvider;
import org.carlspring.strongbox.providers.datastore.StorageProviderRegistry;
import org.carlspring.strongbox.providers.io.RepositoryFileSystemFactory;
import org.carlspring.strongbox.providers.io.RepositoryFileSystemProviderFactory;
import org.carlspring.strongbox.providers.layout.NpmFileSystem;
import org.carlspring.strongbox.providers.layout.NpmFileSystemProvider;
import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
import org.carlspring.strongbox.providers.layout.RepositoryLayoutFileSystemProvider;
import org.carlspring.strongbox.storage.repository.Repository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.dependency.snippet",
                 "org.carlspring.strongbox.event",
                 "org.carlspring.strongbox.repository",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
})
@Import({ EventsConfig.class })
public class NpmLayoutProviderConfig
{

    public static final String FILE_SYSTEM_ALIAS = "RepositoryFileSystemFactory." + NpmLayoutProvider.ALIAS;
    public static final String FILE_SYSTEM_PROVIDER_ALIAS = "RepositoryFileSystemProviderFactory."
            + NpmLayoutProvider.ALIAS;

    @Inject
    protected StorageProviderRegistry storageProviderRegistry;

    @Bean
    public ObjectMapper npmJackasonMapper()
    {
        return new ObjectMapper();
    }

    @Bean(FILE_SYSTEM_PROVIDER_ALIAS)
    public RepositoryFileSystemProviderFactory npmRepositoryFileSystemProviderFactory()
    {
        return (repository) -> {
            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

            RepositoryLayoutFileSystemProvider result = npmFileSystemProvider(storageProvider.getFileSystemProvider());

            return result;
        };

    }

    @Bean
    @Scope("prototype")
    public NpmFileSystemProvider npmFileSystemProvider(FileSystemProvider provider)
    {
        return new NpmFileSystemProvider(provider);
    }

    @Bean(FILE_SYSTEM_ALIAS)
    public RepositoryFileSystemFactory npmRepositoryFileSystemFactory()
    {
        RepositoryFileSystemProviderFactory providerFactory = npmRepositoryFileSystemProviderFactory();
        
        return (repository) -> {
            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

            return npmRepositoryFileSystem(repository, storageProvider.getFileSystem(),
                                           providerFactory.create(repository));
        };
    }

    @Bean
    @Scope("prototype")
    public NpmFileSystem npmRepositoryFileSystem(Repository repository,
                                                 FileSystem storageFileSystem,
                                                 RepositoryLayoutFileSystemProvider provider)
    {
        return new NpmFileSystem(repository, storageFileSystem, provider);
    }

}
