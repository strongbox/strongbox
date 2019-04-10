package org.carlspring.strongbox;

import org.carlspring.strongbox.config.*;
import org.carlspring.strongbox.testing.NullLayoutConfiguration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Import({ MockedRemoteRepositoriesHeartbeatConfig.class,
          TestingCoreConfig.class,
          CommonConfig.class,
          ClientConfig.class,
          DataServiceConfig.class,
          EventsConfig.class,
          StorageCoreConfig.class,
          StorageApiConfig.class,
          NullLayoutConfiguration.class
})
public class StorageApiTestConfig
{

}
