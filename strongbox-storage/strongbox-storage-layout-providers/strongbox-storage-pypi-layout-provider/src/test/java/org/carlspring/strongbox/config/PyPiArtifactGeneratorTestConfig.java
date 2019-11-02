package org.carlspring.strongbox.config;

import org.carlspring.strongbox.cron.config.CronTasksConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        CommonConfig.class,
        TestingCoreConfig.class,
        EventsConfig.class,
        CronTasksConfig.class,
        StorageCoreConfig.class,
        StorageApiConfig.class,
        ClientConfig.class,
        PypiLayoutProviderConfig.class,
})
public class PyPiArtifactGeneratorTestConfig {


}
