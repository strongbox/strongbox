package org.carlspring.strongbox.config;

import org.carlspring.strongbox.cron.config.CronTasksConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author sbespalov
 *
 */
@Configuration
@Import({ NpmLayoutProviderTestConfig.class,
          ClientConfig.class,
          EventsConfig.class,
          CronTasksConfig.class
})
public class NpmLayoutProviderCronTasksTestConfig
{

}
