package org.carlspring.strongbox.config;

import org.carlspring.strongbox.MockedRemoteRepositoriesHeartbeatConfig;
import org.carlspring.strongbox.configuration.ConfigurationFileManager;
import org.carlspring.strongbox.configuration.MutableConfiguration;
import org.carlspring.strongbox.cron.config.CronTasksConfig;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.testing.artifact.ArtifactResolutionServiceHelper;
import org.carlspring.strongbox.yaml.YAMLMapperFactory;

import java.io.IOException;

import org.mockito.Mockito;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
import static org.mockito.ArgumentMatchers.any;

@Configuration
@Import({ CommonConfig.class,
          TestingCoreConfig.class,
          EventsConfig.class,
          DataServiceConfig.class,
          CronTasksConfig.class,
          StorageCoreConfig.class,
          StorageApiConfig.class,
          PypiLayoutProviderConfig.class,
          MockedRemoteRepositoriesHeartbeatConfig.class,
          ClientConfig.class })
public class PypiLayoutProviderTestConfig
{

    @Bean(name = "mockedConfigurationFileManager")
    @Primary
    ConfigurationFileManager configurationFileManager(YAMLMapperFactory yamlMapperFactory)
            throws IOException
    {
        final ConfigurationFileManager configurationFileManager = Mockito.spy(
                new ConfigurationFileManager(yamlMapperFactory));

        Mockito.doNothing().when(configurationFileManager).store(any(MutableConfiguration.class));

        return configurationFileManager;
    }

    @Bean
    @Conditional(CronRelatedBeansAreMissedInContext.class)
    CronTaskConfigurationService cronTaskConfigurationService()
    {
        return Mockito.mock(CronTaskConfigurationService.class);
    }

    @Bean
    @Conditional(CronRelatedBeansAreMissedInContext.class)
    CronJobSchedulerService cronJobSchedulerService()
    {
        return Mockito.mock(CronJobSchedulerService.class);
    }

    @Bean
    public ArtifactResolutionServiceHelper artifactResolutionServiceHelper()
    {
        return new ArtifactResolutionServiceHelper();
    }

    private static class CronRelatedBeansAreMissedInContext
            implements Condition
    {

        @Override
        public boolean matches(ConditionContext context,
                               AnnotatedTypeMetadata metadata)
        {
            return !context.getRegistry().containsBeanDefinition("cronTaskConfigurationServiceImpl") &&
                   !context.getRegistry().containsBeanDefinition("cronJobSchedulerServiceImpl");
        }
    }

}
