package org.carlspring.strongbox.config;

import org.carlspring.strongbox.booters.ResourcesBooter;
import org.carlspring.strongbox.booters.StorageBooter;
import org.carlspring.strongbox.storage.checksum.CalculatedChecksumCacheManager;
import org.carlspring.strongbox.storage.validation.ArtifactCoordinatesValidator;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.artifact",
                 "org.carlspring.strongbox.configuration",
                 "org.carlspring.strongbox.io",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
                 "org.carlspring.strongbox.xml",
                 "org.carlspring.strongbox.dependency",
                 "org.carlspring.strongbox.domain",
                 "org.carlspring.strongbox.booters"
})
@EnableAsync
public class StorageApiConfig
{

    @Inject
    private List<ArtifactCoordinatesValidator> versionValidators;

    @Bean(name = "calculatedChecksumCacheManager")
    CalculatedChecksumCacheManager calculatedChecksumCacheManager()
    {
        CalculatedChecksumCacheManager calculatedChecksumCacheManager = new CalculatedChecksumCacheManager();
        calculatedChecksumCacheManager.setCachedChecksumExpiredCheckInterval(300000);
        calculatedChecksumCacheManager.setCachedChecksumLifetime(60000);

        return calculatedChecksumCacheManager;
    }

    @Bean(name = "versionValidators")
    LinkedHashSet<ArtifactCoordinatesValidator> versionValidators()
    {
        return new LinkedHashSet<>(versionValidators);
    }

    @Bean(name = "resourcesBooter")
    ResourcesBooter getResourcesBooter()
    {
        return new ResourcesBooter();
    }

    @Bean(name = "storageBooter")
    StorageBooter getStorageBooter()
    {
        return new StorageBooter();
    }

}
