package org.carlspring.strongbox.cron.context;

import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.cron.config.CronTasksConfig;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.common.base.Throwables;
import org.mockito.Mockito;
import org.quartz.SchedulerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import static org.mockito.Matchers.any;

/**
 * @author Martin Todorov
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(classes = { CronTasksConfig.class,
                                  WebConfig.class,
                                  CronTaskRestTest.MockedCronJobSchedulerConfig.class })
@WebAppConfiguration
@WithUserDetails(value = "admin")
public @interface CronTaskRestTest
{

    @Configuration
    class MockedCronJobSchedulerConfig
    {

        @Primary
        @Bean
        CronJobSchedulerService cronJobSchedulerService()
        {
            CronJobSchedulerService scheduler = Mockito.mock(CronJobSchedulerService.class);
            try
            {
                Mockito.doNothing().when(scheduler).executeJob(any(CronTaskConfiguration.class));
            }
            catch (SchedulerException ex)
            {
                throw Throwables.propagate(ex);
            }
            return scheduler;
        }

    }

}
