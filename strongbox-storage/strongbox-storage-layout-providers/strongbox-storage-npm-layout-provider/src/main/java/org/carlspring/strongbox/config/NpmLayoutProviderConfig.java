package org.carlspring.strongbox.config;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.npm.metadata.jackson.NpmJacksonMapperFactory;
import org.carlspring.strongbox.providers.datastore.StorageProvider;
import org.carlspring.strongbox.providers.datastore.StorageProviderRegistry;
import org.carlspring.strongbox.providers.io.LayoutFileSystemFactory;
import org.carlspring.strongbox.providers.io.LayoutFileSystemProviderFactory;
import org.carlspring.strongbox.providers.layout.LayoutFileSystemProvider;
import org.carlspring.strongbox.providers.layout.NpmFileSystem;
import org.carlspring.strongbox.providers.layout.NpmFileSystemProvider;
import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.nio.file.FileSystem;
import java.nio.file.spi.FileSystemProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.dependency.snippet",
                 "org.carlspring.strongbox.repository",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
})
public class NpmLayoutProviderConfig
{

    public static final String FILE_SYSTEM_ALIAS = "LayoutFileSystemFactory." + NpmLayoutProvider.ALIAS;
    public static final String FILE_SYSTEM_PROVIDER_ALIAS = "LayoutFileSystemProviderFactory."
            + NpmLayoutProvider.ALIAS;

    @Inject
    protected StorageProviderRegistry storageProviderRegistry;

    @Bean
    @NpmObjectMapper
    public ObjectMapper npmJacksonMapper()
    {
        return NpmJacksonMapperFactory.createObjectMapper();
    }

    @Bean(FILE_SYSTEM_PROVIDER_ALIAS)
    public LayoutFileSystemProviderFactory npmRepositoryFileSystemProviderFactory()
    {
        return (repository) -> {
            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

            LayoutFileSystemProvider result = npmFileSystemProvider(storageProvider.getFileSystemProvider());

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
    public LayoutFileSystemFactory npmRepositoryFileSystemFactory(PropertiesBooter propertiesBooter)
    {
        LayoutFileSystemProviderFactory providerFactory = npmRepositoryFileSystemProviderFactory();
        
        return (repository) -> {
            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

            return npmRepositoryFileSystem(propertiesBooter, repository, storageProvider.getFileSystem(),
                                           providerFactory.create(repository));
        };
    }

    @Bean
    @Scope("prototype")
    public NpmFileSystem npmRepositoryFileSystem(PropertiesBooter propertiesBooter,
                                                 Repository repository,
                                                 FileSystem storageFileSystem,
                                                 LayoutFileSystemProvider provider)
    {
        return new NpmFileSystem(propertiesBooter, repository, storageFileSystem, provider);
    }
    
    @Documented
    @Qualifier
    @Retention(RUNTIME)
    public @interface NpmObjectMapper
    {
        
    }

}
