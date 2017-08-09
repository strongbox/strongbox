package org.carlspring.strongbox.cron.context;

import org.carlspring.strongbox.config.ClientConfig;
import org.carlspring.strongbox.config.EventsConfig;
import org.carlspring.strongbox.config.StorageApiConfig;
import org.carlspring.strongbox.cron.config.CronTasksConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.context.ContextConfiguration;

/**
 * @author Alex Oreshkevich
 * @author Martin Todorov
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(classes = { StorageApiConfig.class,
                                  ClientConfig.class,
                                  EventsConfig.class,
                                  CronTasksConfig.class })
public @interface CronTaskTest
{

}
