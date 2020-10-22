package org.carlspring.strongbox.config;

import org.carlspring.strongbox.MockedRemoteRepositoriesHeartbeatConfig;
import org.carlspring.strongbox.cron.config.CronTasksConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author sbespalov
 *
 */
@Configuration
@Import({ TestingCoreConfig.class,
          EventsConfig.class,
          DataServiceConfig.class,
          CommonConfig.class,
          CronTasksConfig.class,
          StorageCoreConfig.class,
          StorageApiConfig.class,
          NpmLayoutProviderConfig.class,
          MockedRemoteRepositoriesHeartbeatConfig.class,
          ClientConfig.class
})
public class NpmLayoutProviderTestConfig
{

}
