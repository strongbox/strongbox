package org.carlspring.strongbox.config;

import org.carlspring.strongbox.MockedRemoteRepositoriesHeartbeatConfig;
import org.carlspring.strongbox.app.StrongboxSpringBootApplication;
import org.carlspring.strongbox.configuration.ConfigurationFileManager;
import org.carlspring.strongbox.configuration.MutableConfiguration;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.rest.common.RestAssuredTestExecutionListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.web.WebAppConfiguration;
import static org.mockito.ArgumentMatchers.any;


/**
 * Helper meta annotation for all rest-assured based tests. Specifies tests that
 * require web server and remote HTTP protocol.
 *
 * @author Alex Oreshkevich
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { StrongboxSpringBootApplication.class,
                            MockedRemoteRepositoriesHeartbeatConfig.class,
                            IntegrationTest.TestConfig.class,
                            RestAssuredConfig.class})
@WebAppConfiguration("classpath:")
@WithUserDetails(value = "admin")
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { RestAssuredTestExecutionListener.class,
                                      CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public @interface IntegrationTest
{

    @Configuration
    class TestConfig
    {

        @Bean
        @Primary
        CronTaskConfigurationService cronTaskConfigurationService()
        {
            return Mockito.mock(CronTaskConfigurationService.class);
        }

        @Bean
        @Primary
        CronJobSchedulerService cronJobSchedulerService()
        {
            return Mockito.mock(CronJobSchedulerService.class);
        }

        @Bean
        @Primary
        ConfigurationFileManager configurationFileManager()
        {
            final ConfigurationFileManager configurationFileManager = Mockito.spy(new ConfigurationFileManager());

            Mockito.doNothing()
                   .when(configurationFileManager)
                   .store(any(MutableConfiguration.class));

            return configurationFileManager;
        }
    }

}
