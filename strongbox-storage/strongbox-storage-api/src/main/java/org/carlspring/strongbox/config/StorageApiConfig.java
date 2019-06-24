package org.carlspring.strongbox.config;

import org.carlspring.strongbox.booters.StorageBooter;
import org.carlspring.strongbox.booters.TempDirBooter;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.validation.ArtifactCoordinatesValidator;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.artifact",
                 "org.carlspring.strongbox.configuration",
                 "org.carlspring.strongbox.io",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
                 "org.carlspring.strongbox.yaml",
                 "org.carlspring.strongbox.dependency",
                 "org.carlspring.strongbox.domain",
                 "org.carlspring.strongbox.booters"
})
@EnableAsync
public class StorageApiConfig
{

    @Inject
    private List<ArtifactCoordinatesValidator> versionValidators;

    @Bean
    ChecksumCacheManager checksumCacheManager()
    {
        ChecksumCacheManager checksumCacheManager = new ChecksumCacheManager();
        checksumCacheManager.setCachedChecksumExpiredCheckInterval(300000);
        checksumCacheManager.setCachedChecksumLifetime(60000);

        return checksumCacheManager;
    }

    @Bean
    LinkedHashSet<ArtifactCoordinatesValidator> versionValidators()
    {
        return new LinkedHashSet<>(versionValidators);
    }

    @Bean
    TempDirBooter tempDirBooter()
    {
        return new TempDirBooter();
    }
    
    @Bean
    @DependsOn("tempDirBooter")
    StorageBooter storageBooter()
    {
        return new StorageBooter();
    }

}
