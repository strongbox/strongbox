package org.carlspring.strongbox.config;

import org.carlspring.strongbox.CommonConfig;
import org.carlspring.strongbox.StorageApiConfig;
import org.carlspring.strongbox.StorageIndexingConfig;
import org.carlspring.strongbox.configuration.StrongboxSecurityConfig;
import org.carlspring.strongbox.users.UsersConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
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
                 DataServiceConfig.class,
                 CommonConfig.class,
                 StrongboxSecurityConfig.class,
                 StorageIndexingConfig.class,
                 StorageApiConfig.class,
                 UsersConfig.class,
                 SecurityConfig.class
         })
@EnableCaching
public class WebConfig
{

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    public WebConfig()
    {
        logger.info("WebConfig init...");
    }
}
