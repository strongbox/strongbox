package org.carlspring.strongbox.cron.context;

import org.carlspring.strongbox.config.*;
import org.carlspring.strongbox.cron.config.CronTasksConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author Alex Oreshkevich
 * @author Martin Todorov
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(classes = { StorageApiConfig.class,
                                  ClientConfig.class,
                                  EventsConfig.class,
                                  Maven2LayoutProviderConfig.class,
                                  NugetLayoutProviderConfig.class,
                                  P2LayoutProviderConfig.class,
                                  CronTasksConfig.class })
@WebAppConfiguration
public @interface CronTaskTest
{

}
