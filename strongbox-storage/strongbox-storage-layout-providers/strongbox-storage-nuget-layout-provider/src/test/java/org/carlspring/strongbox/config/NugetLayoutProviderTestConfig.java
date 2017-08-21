package org.carlspring.strongbox.config;

import org.carlspring.strongbox.MockedRemoteRepositoriesHeartbeatConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
          TestingCoreConfig.class
})
public class NugetLayoutProviderTestConfig
{

}
