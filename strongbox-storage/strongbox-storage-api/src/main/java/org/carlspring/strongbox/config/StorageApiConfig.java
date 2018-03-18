package org.carlspring.strongbox.config;

import org.carlspring.strongbox.booters.ResourcesBooter;
import org.carlspring.strongbox.booters.StorageBooter;
import org.carlspring.strongbox.configuration.ConfigurationFileManager;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.validation.ArtifactCoordinatesValidator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.collections.MapUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.artifact",
                 "org.carlspring.strongbox.configuration",
                 "org.carlspring.strongbox.io",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
                 "org.carlspring.strongbox.xml",
                 "org.carlspring.strongbox.dependency"
})
public class StorageApiConfig
{

    @Inject
    private List<ArtifactCoordinatesValidator> versionValidators;

    @Inject
    private TransactionTemplate transactionTemplate;

    @Inject
    private ConfigurationFileManager configurationFileManager;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    @PostConstruct
    public void init()
    {
        transactionTemplate.execute((s) ->
                                    {
                                        doInit();
                                        return null;
                                    });
    }

    private void doInit()
    {
        final org.carlspring.strongbox.configuration.Configuration configuration = configurationFileManager.read();
        setProxyRepositoryConnectionPoolConfigurations(configuration);
        configurationManagementService.save(configuration);
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


    private void setProxyRepositoryConnectionPoolConfigurations(final org.carlspring.strongbox.configuration.Configuration configuration)
    {
        configuration.getStorages().values().stream()
                     .filter(storage -> MapUtils.isNotEmpty(storage.getRepositories()))
                     .flatMap(storage -> storage.getRepositories().values().stream())
                     .filter(repository -> repository.getHttpConnectionPool() != null &&
                                           repository.getRemoteRepository() != null &&
                                           repository.getRemoteRepository().getUrl() != null)
                     .forEach(repository -> proxyRepositoryConnectionPoolConfigurationService.setMaxPerRepository(
                             repository.getRemoteRepository().getUrl(),
                             repository.getHttpConnectionPool().getAllocatedConnections()));
    }

}
