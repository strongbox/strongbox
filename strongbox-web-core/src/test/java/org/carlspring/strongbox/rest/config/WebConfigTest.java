package org.carlspring.strongbox.rest.config;

import org.carlspring.maven.artifact.downloader.IndexDownloader;
import org.carlspring.strongbox.config.ClientConfig;
import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.config.SecurityConfig;
import org.carlspring.strongbox.config.StorageApiConfig;
import org.carlspring.strongbox.config.StorageIndexingConfig;
import org.carlspring.strongbox.config.UsersConfig;
import org.carlspring.strongbox.configuration.StrongboxSecurityConfig;
import org.carlspring.strongbox.cron.config.CronTasksConfig;
import org.carlspring.strongbox.mapper.CustomJaxb2RootElementHttpMessageConverter;
import org.carlspring.strongbox.rest.common.RestAssuredIndexDownloader;
import org.carlspring.strongbox.utils.CustomAntPathMatcher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Kate Novik.
 */
@Configuration
@ComponentScan({ "org.carlspring.strongbox.controller",
                 "org.carlspring.strongbox.mapper",
                 "org.carlspring.strongbox.security",
                 "org.carlspring.strongbox.authentication",
                 "org.carlspring.strongbox.user",
                 "org.carlspring.strongbox.utils",
                 "org.carlspring.logging" })
@Import({ CommonConfig.class,
          StrongboxSecurityConfig.class,
          StorageIndexingConfig.class,
          StorageApiConfig.class,
          UsersConfig.class,
          SecurityConfig.class,
          ClientConfig.class,
          CronTasksConfig.class })
@EnableCaching
@EnableWebMvc
public class WebConfigTest
        extends WebMvcConfigurerAdapter
{

    private static final Logger logger = LoggerFactory.getLogger(WebConfigTest.class);

    @Inject
    CustomJaxb2RootElementHttpMessageConverter jaxb2RootElementHttpMessageConverter;

    @Inject
    @Named("customAntPathMatcher")
    CustomAntPathMatcher antPathMatcher;

    @Inject
    ObjectMapper objectMapper;

    public WebConfigTest()
    {
        logger.debug("Initialized web configuration for tests.");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
    {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
        stringConverter.setWriteAcceptCharset(false);

        converters.add(new ByteArrayHttpMessageConverter()); // if your argument is a byte[]
        converters.add(stringConverter);
        converters.add(new FormHttpMessageConverter());
        converters.add(jackson2Converter());
        converters.add(jaxb2RootElementHttpMessageConverter);
        converters.add(new ResourceHttpMessageConverter());
    }

    @Bean
    public MappingJackson2HttpMessageConverter jackson2Converter()
    {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }

    @Bean
    public IndexDownloader indexDownloader()
            throws PlexusContainerException, ComponentLookupException
    {
        return new RestAssuredIndexDownloader();
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer)
    {
        configurer.setUseSuffixPatternMatch(true)
                  .setUseTrailingSlashMatch(false)
                  .setUseRegisteredSuffixPatternMatch(true)
                  .setPathMatcher(antPathMatcher);
    }

}
