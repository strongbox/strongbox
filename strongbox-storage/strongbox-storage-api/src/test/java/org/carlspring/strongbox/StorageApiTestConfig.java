package org.carlspring.strongbox;

import org.carlspring.strongbox.config.ClientConfig;
import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.EventsConfig;
import org.carlspring.strongbox.config.StorageApiConfig;
import org.carlspring.strongbox.config.StorageCoreConfig;
import org.carlspring.strongbox.testing.NullLayoutConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Import({ MockedRemoteRepositoriesHeartbeatConfig.class,
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
