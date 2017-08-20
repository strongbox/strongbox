package org.carlspring.strongbox.controllers.context;

import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mockito.Mockito;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Helper meta annotation for all rest-assured based tests. Specifies tests that require web server and remote HTTP protocol.
 *
 * @author Alex Oreshkevich
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(classes = { WebConfig.class,
                                  IntegrationTest.TestConfig.class })
@WebAppConfiguration
@WithUserDetails(value = "admin")
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