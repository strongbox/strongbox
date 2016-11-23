package org.carlspring.strongbox.config;

import org.carlspring.strongbox.StorageIndexingConfig;
import org.carlspring.strongbox.configuration.StrongboxSecurityConfig;
import org.carlspring.strongbox.mapper.CustomJaxb2RootElementHttpMessageConverter;
import org.carlspring.strongbox.utils.CustomUrlPathHelper;

import javax.inject.Inject;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@ComponentScan({ "org.carlspring.strongbox",
                 "org.carlspring.logging" })
@Import({ CommonConfig.class,
          StrongboxSecurityConfig.class,
          StorageIndexingConfig.class,
          StorageApiConfig.class,
          UsersConfig.class,
          SecurityConfig.class,
          ClientConfig.class })
@EnableCaching
@EnableWebMvc
public class WebConfig
        extends WebMvcConfigurationSupport
{

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Inject
    CustomJaxb2RootElementHttpMessageConverter jaxb2RootElementHttpMessageConverter;

    @Inject
    CustomUrlPathHelper customUrlPathHelper;

    @Inject
    ObjectMapper objectMapper;

    public WebConfig()
    {
        logger.debug("Initialized web configuration.");
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
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        RequestMappingHandlerMapping mapping = super.requestMappingHandlerMapping();
        mapping.setUseSuffixPatternMatch(false);
        mapping.setUrlPathHelper(customUrlPathHelper);
        return mapping;
    }
}
