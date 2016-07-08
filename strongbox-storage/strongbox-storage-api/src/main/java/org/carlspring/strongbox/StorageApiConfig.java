package org.carlspring.strongbox;

import org.carlspring.strongbox.services.impl.ArtifactResolutionServiceImpl;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.resolvers.LocationResolverRegistry;
import org.carlspring.strongbox.storage.validation.version.VersionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.orient.commons.repository.config.EnableOrientRepositories;

import java.util.LinkedHashSet;
import java.util.List;

@Configuration
@ComponentScan({
        "org.carlspring.strongbox.artifact",
        "org.carlspring.strongbox.configuration",
        "org.carlspring.strongbox.io",
        "org.carlspring.strongbox.services",
        "org.carlspring.strongbox.storage",
        "org.carlspring.strongbox.storage.resolvers",
        "org.carlspring.strongbox.xml"
})
@EnableOrientRepositories(basePackages = "org.carlspring.strongbox.storage.repository")
public class StorageApiConfig
{
    private static final Logger logger = LoggerFactory.getLogger(StorageApiConfig.class);

    /*
    @Autowired
    private FSLocationResolver fsLocationResolver;

    @Autowired
    private ProxyLocationResolver proxyLocationResolver;

    @Autowired
    private GroupLocationResolver groupLocationResolver;
    */

    @Autowired
    private List<VersionValidator> versionValidators;

    @Autowired
    private LocationResolverRegistry locationResolverRegistry;

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

    /*
    @Bean(name = "resolvers")
    LinkedHashMap<String, LocationResolver> resolvers()
    {
        LinkedHashMap<String, LocationResolver> resolvers = new LinkedHashMap<>();
        resolvers.put("file-system", fsLocationResolver);
        resolvers.put("proxy", proxyLocationResolver);
        resolvers.put("group", groupLocationResolver);

        return resolvers;
    }
    */

    /*
    @Bean(name = "artifactResolutionService", initMethod = "listResolvers")
    ArtifactResolutionServiceImpl artifactResolutionService()
    {
        ArtifactResolutionServiceImpl artifactResolutionService = new ArtifactResolutionServiceImpl();

        return artifactResolutionService;
    }
    */

    @Bean(name = "versionValidators")
    LinkedHashSet<VersionValidator> versionValidators()
    {
        return new LinkedHashSet<>(versionValidators);
    }

}
