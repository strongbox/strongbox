package org.carlspring.strongbox.config;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.search.OrientDbSearchProvider;
import org.carlspring.strongbox.providers.search.SearchProviderRegistry;
import org.carlspring.strongbox.services.impl.ArtifactResolutionServiceImpl;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.validation.version.VersionValidator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.List;

import com.orientechnologies.orient.core.entity.OEntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.artifact",
                 "org.carlspring.strongbox.configuration",
                 "org.carlspring.strongbox.io",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
                 "org.carlspring.strongbox.xml"
               })
public class StorageApiConfig
{

    @Inject
    private List<VersionValidator> versionValidators;

    @Inject
    private ArtifactResolutionServiceImpl artifactResolutionService;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private OEntityManager entityManager;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private SearchProviderRegistry searchProviderRegistry;

    @Inject
    private OrientDbSearchProvider orientDbSearchProvider;


    @PostConstruct
    public void init()
    {
        // register all domain entities
        entityManager.registerEntityClasses(ArtifactEntry.class.getPackage()
                                                               .getName());
    }

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
