package org.carlspring.strongbox.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author carlspring
 */
@Configuration
@Import({ RawLayoutProviderConfig.class,
          EventsConfig.class,
          StorageCoreConfig.class,
          StorageApiConfig.class,
          CommonConfig.class,
          ClientConfig.class,
          DataServiceConfig.class,
          TestingCoreConfig.class
        })
public class RawLayoutProviderTestConfig
{
    
}
