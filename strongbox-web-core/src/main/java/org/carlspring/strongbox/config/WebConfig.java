package org.carlspring.strongbox.config;

import org.carlspring.strongbox.StorageIndexingConfig;
import org.carlspring.strongbox.configuration.StrongboxSecurityConfig;
import org.carlspring.strongbox.mapper.CustomJaxb2RootElementHttpMessageConverter;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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
        extends WebMvcConfigurerAdapter
{

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Inject
    CustomJaxb2RootElementHttpMessageConverter jaxb2RootElementHttpMessageConverter;

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
        converters.add(new MappingJackson2HttpMessageConverter());
        converters.add(jaxb2RootElementHttpMessageConverter);
        converters.add(new ResourceHttpMessageConverter());
    }
}
