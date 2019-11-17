package org.carlspring.strongbox.config;

import org.carlspring.strongbox.MockedRemoteRepositoriesHeartbeatConfig;
import org.carlspring.strongbox.configuration.ConfigurationFileManager;
import org.carlspring.strongbox.configuration.MutableConfiguration;
import org.carlspring.strongbox.cron.config.CronTasksConfig;
import org.carlspring.strongbox.yaml.YAMLMapperFactory;
//import org.carlspring.strongbox.cron.config.CronTasksConfig;

import java.io.IOException;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import static org.mockito.ArgumentMatchers.any;

/**
 * @author carlspring
 */
@Configuration
@Import({ CommonConfig.class,
          TestingCoreConfig.class,
          EventsConfig.class,
          DataServiceConfig.class,
          CronTasksConfig.class,
          StorageCoreConfig.class,
          StorageApiConfig.class,
          RpmLayoutProviderConfig.class,
          MockedRemoteRepositoriesHeartbeatConfig.class,
          ClientConfig.class
})
public class RpmLayoutProviderTestConfig
{

    @Bean(name = "mockedConfigurationFileManager")
    @Primary
    ConfigurationFileManager configurationFileManager(YAMLMapperFactory yamlMapperFactory) throws IOException
    {
        final ConfigurationFileManager configurationFileManager = Mockito.spy(new ConfigurationFileManager(yamlMapperFactory));

        Mockito.doNothing().when(configurationFileManager).store(any(MutableConfiguration.class));

        return configurationFileManager;
    }

}
