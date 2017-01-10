package org.carlspring.strongbox.cron.context;

import org.carlspring.strongbox.config.ClientConfig;
import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.StorageApiConfig;
import org.carlspring.strongbox.config.StorageIndexingConfig;
import org.carlspring.strongbox.config.UsersConfig;
import org.carlspring.strongbox.configuration.StrongboxSecurityConfig;
import org.carlspring.strongbox.cron.config.CronTasksConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.SecurityConfig;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Marks test to be executed only under special spring active profile.
 *
 * @author Alex Oreshkevich
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(classes = { CronTasksConfig.class,
                                  StorageIndexingConfig.class,
                                  StorageApiConfig.class,
                                  CommonConfig.class,
                                  ClientConfig.class,
                                  DataServiceConfig.class,
                                  UsersConfig.class,
                                  StrongboxSecurityConfig.class })
@WebAppConfiguration
//@WithUserDetails(value = "admin")
@IfProfileValue(name = "spring.profiles.active", values = { "quartz-integration-test" })
public @interface CronTaskTest
{

}
