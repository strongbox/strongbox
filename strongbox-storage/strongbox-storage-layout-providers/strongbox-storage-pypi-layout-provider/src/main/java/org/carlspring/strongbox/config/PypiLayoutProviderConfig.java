package org.carlspring.strongbox.config;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.providers.datastore.StorageProvider;
import org.carlspring.strongbox.providers.datastore.StorageProviderRegistry;
import org.carlspring.strongbox.providers.io.LayoutFileSystemFactory;
import org.carlspring.strongbox.providers.io.LayoutFileSystemProviderFactory;
import org.carlspring.strongbox.providers.layout.LayoutFileSystemProvider;
import org.carlspring.strongbox.providers.layout.PypiFileSystem;
import org.carlspring.strongbox.providers.layout.PypiFileSystemProvider;
import org.carlspring.strongbox.providers.layout.PypiLayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.nio.file.FileSystem;
import java.nio.file.spi.FileSystemProvider;

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
                 "org.carlspring.strongbox.storage" })
public class PypiLayoutProviderConfig
{

    public static final String FILE_SYSTEM_ALIAS = "LayoutFileSystemFactory." + PypiLayoutProvider.ALIAS;

    public static final String FILE_SYSTEM_PROVIDER_ALIAS = "LayoutFileSystemProviderFactory." +
                                                            PypiLayoutProvider.ALIAS;

    @Inject
    protected StorageProviderRegistry storageProviderRegistry;


    @Bean(FILE_SYSTEM_PROVIDER_ALIAS)
    public LayoutFileSystemProviderFactory pypiRepositoryFileSystemProviderFactory()
    {
        return (repository) -> {
            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

            LayoutFileSystemProvider result = pypiFileSystemProvider(storageProvider.getFileSystemProvider());

            return result;
        };

    }


    @Bean
    @Scope("prototype")
    public PypiFileSystemProvider pypiFileSystemProvider(FileSystemProvider provider)
    {
        return new PypiFileSystemProvider(provider);
    }

    @Bean(FILE_SYSTEM_ALIAS)
    public LayoutFileSystemFactory pypiRepositoryFileSystemFactory(PropertiesBooter propertiesBooter)
    {
        LayoutFileSystemProviderFactory providerFactory = pypiRepositoryFileSystemProviderFactory();

        return (repository) -> {
            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

            return pypiRepositoryFileSystem(propertiesBooter,
                                            repository,
                                            storageProvider.getFileSystem(),
                                            providerFactory.create(repository));
        };
    }

    @Bean
    @Scope("prototype")
    public PypiFileSystem pypiRepositoryFileSystem(PropertiesBooter propertiesBooter,
                                                   Repository repository,
                                                   FileSystem storageFileSystem,
                                                   LayoutFileSystemProvider provider)
    {
        return new PypiFileSystem(propertiesBooter, repository, storageFileSystem, provider);
    }


    @Documented
    @Qualifier
    @Retention(RUNTIME)
    public @interface PypiObjectMapper
    {

    }

}
