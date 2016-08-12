package org.carlspring.strongbox.config;

import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.repository.RepositoryProviderRegistry;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.services.impl.ArtifactResolutionServiceImpl;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.validation.version.VersionValidator;

import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.orient.commons.repository.config.EnableOrientRepositories;

@Configuration
@ComponentScan({
                       "org.carlspring.strongbox.artifact",
                       "org.carlspring.strongbox.configuration",
                       "org.carlspring.strongbox.io",
                       "org.carlspring.strongbox.providers",
                       "org.carlspring.strongbox.services",
                       "org.carlspring.strongbox.storage",
                       "org.carlspring.strongbox.storage.resolvers",
                       "org.carlspring.strongbox.xml"
               })
@EnableOrientRepositories(basePackages = "org.carlspring.strongbox.storage.repository")
public class StorageApiConfig
{

    @Autowired
    private List<VersionValidator> versionValidators;

    @Autowired
    private StorageProviderRegistry storageProviderRegistry;

    @Autowired
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Autowired
    private LayoutProviderRegistry layoutProviderRegistry;

    @Autowired
    private ArtifactResolutionServiceImpl artifactResolutionService;


    @Bean(name = "checksumCacheManager")
    ChecksumCacheManager checksumCacheManager()
    {
        ChecksumCacheManager checksumCacheManager = new ChecksumCacheManager();
        checksumCacheManager.setCachedChecksumExpiredCheckInterval(300000);
        checksumCacheManager.setCachedChecksumLifetime(60000);

        return checksumCacheManager;
    }

    @Bean(name = "versionValidators")
    LinkedHashSet<VersionValidator> versionValidators()
    {
        return new LinkedHashSet<>(versionValidators);
    }

}
