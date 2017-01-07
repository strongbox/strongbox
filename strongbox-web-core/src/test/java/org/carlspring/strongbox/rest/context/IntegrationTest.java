package org.carlspring.strongbox.rest.context;

import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.cron.config.CronTasksConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
@ContextConfiguration(classes = { CronTasksConfig.class,
                                  WebConfig.class })
@WebAppConfiguration
@WithUserDetails(value = "admin")
@Rollback
public @interface IntegrationTest
{

}