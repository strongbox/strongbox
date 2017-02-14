package org.carlspring.strongbox.config;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.data.service.NoProxyOrientRepositoryFactoryBean;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.repository.RepositoryProviderRegistry;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.services.impl.ArtifactResolutionServiceImpl;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.validation.version.VersionValidator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static com.orientechnologies.orient.core.entity.OEntityManager.getEntityManagerByDatabaseURL;

import java.util.LinkedHashSet;
import java.util.List;

import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.orient.commons.repository.config.EnableOrientRepositories;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.artifact",
                 "org.carlspring.strongbox.configuration",
                 "org.carlspring.strongbox.io",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
                 "org.carlspring.strongbox.storage.resolvers",
                 "org.carlspring.strongbox.xml"
               })
@EnableOrientRepositories(basePackages = { "org.carlspring.strongbox.storage.repository",
                                           "org.carlspring.strongbox.repository" },
                          repositoryFactoryBeanClass = NoProxyOrientRepositoryFactoryBean.class)
public class StorageApiConfig
{

    @Inject
    private List<VersionValidator> versionValidators;

    @Inject
    private StorageProviderRegistry storageProviderRegistry;

    @Inject
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private ArtifactResolutionServiceImpl artifactResolutionService;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private OEntityManager entityManager;
    
    @PostConstruct
    public void init()
    {
        // register all domain entities
        entityManager.registerEntityClasses(ArtifactEntry.class.getPackage()
                                                               .getName());

        // unable to replace with more generic one (ArtifactCoordinates) because of
        // internal OrientDB exception: MavenArtifactCoordinates will not be serializable because
        // it was not registered using registerEntityClass()
        entityManager.registerEntityClass(MavenArtifactCoordinates.class);
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
