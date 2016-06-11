package org.carlspring.strongbox;

import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.services.impl.ArtifactResolutionServiceImpl;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.resolvers.FSLocationResolver;
import org.carlspring.strongbox.storage.resolvers.GroupLocationResolver;
import org.carlspring.strongbox.storage.resolvers.LocationResolver;
import org.carlspring.strongbox.storage.resolvers.ProxyLocationResolver;
import org.carlspring.strongbox.storage.validation.version.VersionValidator;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan({
        "org.carlspring.strongbox.artifact",
        "org.carlspring.strongbox.configuration",
        "org.carlspring.strongbox.io",
        "org.carlspring.strongbox.services",
        "org.carlspring.strongbox.storage",
        "org.carlspring.strongbox.xml",
        "org.carlspring.strongbox.data"
})
@Import(DataServiceConfig.class)
public class StorageApiConfig
{
    private static final Logger logger = LoggerFactory.getLogger(StorageApiConfig.class);

    @Autowired
    private FSLocationResolver fsLocationResolver;

    @Autowired
    private ProxyLocationResolver proxyLocationResolver;

    @Autowired
    private GroupLocationResolver groupLocationResolver;

    @Autowired
    private List<VersionValidator> versionValidators;


    @Bean(name = "checksumCacheManager")
    ChecksumCacheManager checksumCacheManager()
    {
        ChecksumCacheManager checksumCacheManager = new ChecksumCacheManager();
        checksumCacheManager.setCachedChecksumExpiredCheckInterval(300000);
        checksumCacheManager.setCachedChecksumLifetime(60000);

        return checksumCacheManager;
    }

    @Bean(name = "resolvers")
    LinkedHashMap<String, LocationResolver> resolvers()
    {
        LinkedHashMap<String, LocationResolver> resolvers = new LinkedHashMap<>();
        resolvers.put("file-system", fsLocationResolver);
        resolvers.put("proxy", proxyLocationResolver);
        resolvers.put("group", groupLocationResolver);

        return resolvers;
    }

    @Bean(name = "artifactResolutionService", initMethod = "listResolvers")
    ArtifactResolutionServiceImpl artifactResolutionService()
    {
        ArtifactResolutionServiceImpl artifactResolutionService = new ArtifactResolutionServiceImpl();
        artifactResolutionService.setResolvers(resolvers());

        return artifactResolutionService;
    }

    @Bean(name = "versionValidators")
    LinkedHashSet<VersionValidator> versionValidators()
    {
        return new LinkedHashSet<>(versionValidators);
    }

}
