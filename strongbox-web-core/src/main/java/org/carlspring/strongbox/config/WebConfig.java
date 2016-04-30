package org.carlspring.strongbox.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("org.carlspring.strongbox")
@Import({
        CommonConfig.class,
        SecurityConfig.class,
        StorageIndexingConfig.class,
        StorageApiConfig.class
})
public class WebConfig {
}
