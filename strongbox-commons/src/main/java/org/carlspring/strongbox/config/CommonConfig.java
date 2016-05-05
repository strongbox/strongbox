package org.carlspring.strongbox.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.carlspring.strongbox.common")
public class CommonConfig
{
    private static final Logger logger = LoggerFactory.getLogger(CommonConfig.class);

    public CommonConfig()
    {
        logger.info("Initializing CommonConfig...");
    }
}
