package org.carlspring.strongbox.config;

import org.carlspring.strongbox.MockedRemoteRepositoriesHeartbeatConfig;
import org.carlspring.strongbox.configuration.ConfigurationFileManager;
import org.carlspring.strongbox.configuration.MutableConfiguration;
import org.carlspring.strongbox.cron.config.CronTasksConfig;
import org.carlspring.strongbox.yaml.YAMLMapperFactory;
import org.mockito.Mockito;
import org.springframework.context.annotation.*;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;

@Configuration
@Import({ TestingCoreConfig.class,
        EventsConfig.class,
        DataServiceConfig.class,
        CommonConfig.class,
        StorageCoreConfig.class,
        StorageApiConfig.class,
        PypiLayoutProviderConfig.class,
        MockedRemoteRepositoriesHeartbeatConfig.class,
        ClientConfig.class,
        CronTasksConfig.class })
public class PypiMetadataFileParserTestConfig
{

    @Bean(name = "mockedConfigurationFileManager")
    @Primary
    ConfigurationFileManager configurationFileManager(YAMLMapperFactory yamlMapperFactory)
            throws IOException, JAXBException
    {
        final ConfigurationFileManager configurationFileManager = Mockito.spy(new ConfigurationFileManager(yamlMapperFactory));

        Mockito.doNothing().when(configurationFileManager).store(any(MutableConfiguration.class));

        return configurationFileManager;
    }

}
