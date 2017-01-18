package org.carlspring.strongbox.rest.config;

import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.downloader.IndexDownloader;
import org.carlspring.strongbox.rest.common.RestAssuredIndexDownloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Kate Novik.
 */
@Configuration
@Import({ WebConfig.class })
@EnableCaching
@EnableWebMvc
public class WebConfigTest
        extends WebMvcConfigurerAdapter
{

    private static final Logger logger = LoggerFactory.getLogger(WebConfigTest.class);

    public WebConfigTest()
    {
        logger.debug("Initialized web configuration for tests.");
    }

    @Bean
    public IndexDownloader indexDownloader()
    {
        return new RestAssuredIndexDownloader();
    }

}
