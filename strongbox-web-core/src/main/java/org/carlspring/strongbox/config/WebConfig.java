package org.carlspring.strongbox.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan
({
        "org.carlspring.strongbox",
        "org.carlspring.logging"
})
@Import
({
        CommonConfig.class,
        StrongboxSecurityConfig.class,
        StorageIndexingConfig.class,
        StorageApiConfig.class,
        GlobalSecurityConfig.class,
        SecurityConfig.class
})
public class WebConfig
{
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    public WebConfig()
    {
        logger.info("WebConfig init...");
    }

}
