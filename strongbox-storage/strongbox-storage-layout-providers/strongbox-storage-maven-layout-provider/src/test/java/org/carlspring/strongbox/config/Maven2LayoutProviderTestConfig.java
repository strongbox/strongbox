package org.carlspring.strongbox.config;

import org.carlspring.strongbox.MockedRemoteRepositoriesHeartbeatConfig;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.storage.indexing.downloader.ResourceFetcherFactory;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.maven.index.updater.ResourceFetcher;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Import({ StorageCoreConfig.class,
          StorageApiConfig.class,
          Maven2LayoutProviderConfig.class,
          MockedRemoteRepositoriesHeartbeatConfig.class,
          CommonConfig.class,
          ClientConfig.class,
          DataServiceConfig.class,
          TestingCoreConfig.class,
          StorageApiConfig.class
})
public class Maven2LayoutProviderTestConfig
{

    @Bean
    @Primary
    ResourceFetcherFactory resourceFetcherFactory(ResourceFetcher resourceFetcher)
    {
        final ResourceFetcherFactory resourceFetcherFactory = Mockito.mock(ResourceFetcherFactory.class);

        Mockito.when(resourceFetcherFactory.createIndexResourceFetcher(Matchers.anyString(), Matchers.any(
                CloseableHttpClient.class))).thenReturn(resourceFetcher);

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
