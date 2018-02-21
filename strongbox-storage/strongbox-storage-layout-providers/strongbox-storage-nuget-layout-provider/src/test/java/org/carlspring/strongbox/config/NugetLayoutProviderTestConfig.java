package org.carlspring.strongbox.config;

import org.carlspring.strongbox.MockedRemoteRepositoriesHeartbeatConfig;
import org.carlspring.strongbox.cron.config.CronTasksConfig;
import org.carlspring.strongbox.data.CacheManagerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * @author Martin Todorov
 */
@Configuration
@Import({ NugetLayoutProviderConfig.class,
          StorageCoreConfig.class,
          StorageApiConfig.class,
          MockedRemoteRepositoriesHeartbeatConfig.class,
          CommonConfig.class,
          ClientConfig.class,
          DataServiceConfig.class,
          TestingCoreConfig.class,
          CronTasksConfig.class
})
public class NugetLayoutProviderTestConfig
{

    @Bean
    @Primary
    public CacheManagerConfiguration cacheManagerConfiguration()
    {
        CacheManagerConfiguration cacheManagerConfiguration = new CacheManagerConfiguration();
        cacheManagerConfiguration.setCacheCacheManagerId("nugetLayoutProviderTestCacheManager");
        return cacheManagerConfiguration;
    }
    
}
