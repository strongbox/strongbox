package org.carlspring.strongbox.config;

import org.carlspring.strongbox.cron.config.CronTasksConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Martin Todorov
 */
@Configuration
@Import({ NugetLayoutProviderTestConfig.class,
          ClientConfig.class,
          EventsConfig.class,
          CronTasksConfig.class
})
public class NugetLayoutProviderCronTasksTestConfig
{

}
