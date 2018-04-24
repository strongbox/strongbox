package org.carlspring.strongbox.config;

import org.carlspring.strongbox.MockedRepositoryPathResolverConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author carlspring
 */
@Configuration
@Import({ RawLayoutProviderConfig.class,
          StorageCoreConfig.class,
          StorageApiConfig.class,
          CommonConfig.class,
          ClientConfig.class,
          DataServiceConfig.class,
          TestingCoreConfig.class,
          MockedRepositoryPathResolverConfig.class
        })
public class RawLayoutProviderTestConfig
{
    
}
