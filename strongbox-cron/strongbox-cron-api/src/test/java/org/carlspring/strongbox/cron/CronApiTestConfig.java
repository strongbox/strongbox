package org.carlspring.strongbox.cron;

import org.carlspring.strongbox.config.ClientConfig;
import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.cron.config.CronTasksConfig;
import org.carlspring.strongbox.providers.repository.RepositoryProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import static org.carlspring.strongbox.cron.CronApiTestConfig.TestConfig;

/**
 * @author Przemyslaw Fusik
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(classes = { CommonConfig.class,
                                  ClientConfig.class,
                                  TestConfig.class,
                                  CronTasksConfig.class })
public @interface CronApiTestConfig
{

    @Configuration
    class TestConfig
    {

        @Bean
        Map<String, RepositoryProvider> repositoryProviders()
        {
            return new HashMap<>();
        }
    }
}
