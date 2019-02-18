package org.carlspring.strongbox.config;

import org.carlspring.strongbox.MockedRemoteRepositoriesHeartbeatConfig;
import org.carlspring.strongbox.configuration.ConfigurationFileManager;
import org.carlspring.strongbox.configuration.MutableConfiguration;
import org.carlspring.strongbox.cron.config.CronTasksConfig;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.storage.indexing.downloader.MockedIndexResourceFetcher;
import org.carlspring.strongbox.storage.indexing.downloader.ResourceFetcherFactory;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.maven.index.updater.ResourceFetcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
import static org.mockito.Matchers.any;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Import({ CommonConfig.class,
          TestingCoreConfig.class,
          EventsConfig.class,
          DataServiceConfig.class,
          CronTasksConfig.class,
          StorageCoreConfig.class,
          StorageApiConfig.class,
          Maven2LayoutProviderConfig.class,
          MockedRemoteRepositoriesHeartbeatConfig.class,
          ClientConfig.class
})
public class Maven2LayoutProviderTestConfig
{

    @Bean(name = "mockedConfigurationFileManager")
    @Primary
    ConfigurationFileManager configurationFileManager()
    {
        final ConfigurationFileManager configurationFileManager = Mockito.spy(new ConfigurationFileManager());

        Mockito.doNothing().when(configurationFileManager).store(any(MutableConfiguration.class));

        return configurationFileManager;
    }

    @Bean
    @Primary
    ResourceFetcher mockedIndexResourceFetcher() 
    {
        return new MockedIndexResourceFetcher();
    }
    
    @Bean
    @Primary
    ResourceFetcherFactory mockedResourceFetcherMockFactory(ResourceFetcher resourceFetcher)
    {
        final ResourceFetcherFactory resourceFetcherFactory = Mockito.mock(ResourceFetcherFactory.class);

        Mockito.when(resourceFetcherFactory.createIndexResourceFetcher(ArgumentMatchers.anyString(),
                                                                       ArgumentMatchers.any(CloseableHttpClient.class)))
               .thenReturn(resourceFetcher);

        return resourceFetcherFactory;
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
