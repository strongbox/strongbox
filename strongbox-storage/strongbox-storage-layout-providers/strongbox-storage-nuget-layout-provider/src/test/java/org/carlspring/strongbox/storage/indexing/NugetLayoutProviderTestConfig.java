package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.MockedRemoteRepositoriesHeartbeatConfig;
import org.carlspring.strongbox.config.ClientConfig;
import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.NugetLayoutProviderConfig;
import org.carlspring.strongbox.config.StorageApiConfig;
import org.carlspring.strongbox.config.StorageCoreConfig;
import org.carlspring.strongbox.config.TestingCoreConfig;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Sergey Bespalov
 *
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
