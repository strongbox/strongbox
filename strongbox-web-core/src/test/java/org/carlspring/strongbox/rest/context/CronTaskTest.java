package org.carlspring.strongbox.rest.context;

import org.carlspring.strongbox.cron.config.CronTasksConfig;
import org.carlspring.strongbox.rest.config.WebConfigTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.Rollback;
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
                                  WebConfigTest.class })
@WebAppConfiguration
@WithUserDetails(value = "admin")
@IfProfileValue(name = "spring.profiles.active",
                values = { "quartz-integration-test" })
@Rollback
public @interface CronTaskTest
{

}
