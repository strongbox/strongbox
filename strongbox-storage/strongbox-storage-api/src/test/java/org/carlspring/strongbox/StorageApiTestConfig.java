package org.carlspring.strongbox;

import org.carlspring.strongbox.config.ClientConfig;
import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.EventsConfig;
import org.carlspring.strongbox.config.StorageApiConfig;
import org.carlspring.strongbox.config.StorageCoreConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Import({ StorageCoreConfig.class,
          MockedRemoteRepositoriesHeartbeatConfig.class,
          CommonConfig.class,
          ClientConfig.class,
          DataServiceConfig.class,
          EventsConfig.class,
          StorageApiConfig.class
})
public class StorageApiTestConfig
{
    
    @Bean
    @Primary
    public String ehCacheCacheManagerId()
    {
        return "storageApiTestTestCacheManager";
    }

}
