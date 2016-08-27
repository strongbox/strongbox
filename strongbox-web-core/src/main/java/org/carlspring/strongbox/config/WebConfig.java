package org.carlspring.strongbox.config;

import org.carlspring.strongbox.StorageIndexingConfig;
import org.carlspring.strongbox.configuration.StrongboxSecurityConfig;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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
                 UsersConfig.class,
                 SecurityConfig.class,
                 ClientConfig.class })
@EnableCaching
@EnableWebMvc
public class WebConfig
        extends WebMvcConfigurerAdapter
{

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    public WebConfig()
    {
        logger.debug("Initialized web configuration.");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
    {
        converters.add(new StringHttpMessageConverter());
        converters.add(new FormHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter());
        converters.add(new MappingJackson2XmlHttpMessageConverter());
        converters.add(new ResourceHttpMessageConverter());
    }

    @Bean
    public ObjectMapper objectMapper()
    {
        return new ObjectMapper();
    }
}
