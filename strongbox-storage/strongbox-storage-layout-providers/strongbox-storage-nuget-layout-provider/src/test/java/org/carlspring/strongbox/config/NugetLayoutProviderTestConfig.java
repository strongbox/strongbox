package org.carlspring.strongbox.config;

import org.carlspring.strongbox.MockedRemoteRepositoriesHeartbeatConfig;
import org.carlspring.strongbox.MockedRepositoryPathResolverConfig;
import org.carlspring.strongbox.configuration.ConfigurationFileManager;
import org.carlspring.strongbox.configuration.MutableConfiguration;
import org.carlspring.strongbox.cron.config.CronTasksConfig;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import static org.mockito.Matchers.any;

/**
 * @author Martin Todorov
 */
@Configuration
@Import({ TestingCoreConfig.class,
          DataServiceConfig.class,
          CommonConfig.class,
          StorageCoreConfig.class,
          StorageApiConfig.class,
          NugetLayoutProviderConfig.class,
          MockedRemoteRepositoriesHeartbeatConfig.class,
          MockedRepositoryPathResolverConfig.class,
          ClientConfig.class,
          CronTasksConfig.class
})
public class NugetLayoutProviderTestConfig
{

    @Bean
    @Primary
    ConfigurationFileManager configurationFileManager()
            throws IOException, JAXBException
    {
        final ConfigurationFileManager configurationFileManager = Mockito.spy(new ConfigurationFileManager());

        Mockito.doNothing().when(configurationFileManager).store(
                any(MutableConfiguration.class));

        return configurationFileManager;
    }

}
