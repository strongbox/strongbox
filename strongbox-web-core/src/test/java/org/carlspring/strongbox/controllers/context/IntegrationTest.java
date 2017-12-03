package org.carlspring.strongbox.controllers.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.carlspring.strongbox.MockedRemoteRepositoriesHeartbeatConfig;
import org.carlspring.strongbox.config.RestAssuredConfig;
import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.rest.common.RestAssuredTestExecutionListener;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Helper meta annotation for all rest-assured based tests. Specifies tests that
 * require web server and remote HTTP protocol.
 *
 * @author Alex Oreshkevich
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(classes = { RestAssuredConfig.class,
                                  WebConfig.class,
                                  MockedRemoteRepositoriesHeartbeatConfig.class,
                                  IntegrationTest.TestConfig.class })
@WebAppConfiguration
@WithUserDetails(value = "admin")
@TestExecutionListeners(listeners = RestAssuredTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Rollback
public @interface IntegrationTest
{

    @Configuration
    class TestConfig
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

}
