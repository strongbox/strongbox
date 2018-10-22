package org.carlspring.strongbox.config;

import java.nio.file.FileSystem;
import java.nio.file.spi.FileSystemProvider;

import javax.inject.Inject;

import org.carlspring.strongbox.providers.datastore.StorageProvider;
import org.carlspring.strongbox.providers.datastore.StorageProviderRegistry;
import org.carlspring.strongbox.providers.io.LayoutFileSystemFactory;
import org.carlspring.strongbox.providers.io.LayoutFileSystemProviderFactory;
import org.carlspring.strongbox.providers.layout.IndexedMaven2FileSystemProvider;
import org.carlspring.strongbox.providers.layout.Maven2FileSystemProvider;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.layout.MavenFileSystem;
import org.carlspring.strongbox.providers.layout.LayoutFileSystemProvider;
import org.carlspring.strongbox.storage.repository.Repository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.configuration",
                 "org.carlspring.strongbox.repository",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage" })
@Import(MavenIndexerConfig.class)
public class Maven2LayoutProviderConfig
{

    public static final String FILE_SYSTEM_ALIAS = "RepositoryFileSystemFactory." + Maven2LayoutProvider.ALIAS;
    public static final String FILE_SYSTEM_PROVIDER_ALIAS = "RepositoryFileSystemProviderFactory."
            + Maven2LayoutProvider.ALIAS;

    @Inject
    protected StorageProviderRegistry storageProviderRegistry;

    @Inject
    private Environment environment;

    @Bean(FILE_SYSTEM_PROVIDER_ALIAS)
    public LayoutFileSystemProviderFactory mavenRepositoryFileSystemProviderFactory()
    {
        return (repository) -> {
            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

            LayoutFileSystemProvider result;
            if (Boolean.parseBoolean(environment.getProperty(MavenIndexerEnabledCondition.MAVEN_INDEXER_ENABLED)))
            {
                result = indexedMavenFileSystemProvider(storageProvider.getFileSystemProvider());
            }
            else
            {
                result = mavenFileSystemProvider(storageProvider.getFileSystemProvider());
            }

            return result;
        };

    }

    @Bean
    @Scope("prototype")
    public Maven2FileSystemProvider mavenFileSystemProvider(FileSystemProvider provider)
    {
        return new Maven2FileSystemProvider(provider);
    }

    @Bean
    @Scope("prototype")
    public LayoutFileSystemProvider indexedMavenFileSystemProvider(FileSystemProvider provider)
    {
        return new IndexedMaven2FileSystemProvider(provider);
    }

    @Bean(FILE_SYSTEM_ALIAS)
    public LayoutFileSystemFactory mavenRepositoryFileSystemFactory()
    {
        LayoutFileSystemProviderFactory providerFactory = mavenRepositoryFileSystemProviderFactory();
        
        return (repository) -> {
            StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

            return mavenRepositoryFileSystem(repository, storageProvider.getFileSystem(),
                                             providerFactory.create(repository));
        };
    }

    @Bean
    @Scope("prototype")
    public MavenFileSystem mavenRepositoryFileSystem(Repository repository,
                                                     FileSystem storageFileSystem,
                                                     LayoutFileSystemProvider provider)
    {
        return new MavenFileSystem(repository, storageFileSystem, provider);
    }

}
