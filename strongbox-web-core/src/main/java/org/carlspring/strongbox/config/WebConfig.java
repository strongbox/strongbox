package org.carlspring.strongbox.config;

import org.carlspring.strongbox.configuration.StrongboxSecurityConfig;
import org.carlspring.strongbox.cron.config.CronTasksConfig;
import org.carlspring.strongbox.utils.CustomAntPathMatcher;
import org.carlspring.strongbox.web.HeaderMappingFilter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.Marshaller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@ComponentScan({ "com.carlspring.strongbox.controllers",
                 "org.carlspring.strongbox.controllers",
                 "org.carlspring.strongbox.mapper",
                 "org.carlspring.strongbox.utils",
                 "org.carlspring.logging" })
@Import({ CommonConfig.class,
          StrongboxSecurityConfig.class,
          StorageApiConfig.class,
          Maven2LayoutProviderConfig.class,
          NugetLayoutProviderConfig.class,
          StorageCoreConfig.class,
          SecurityConfig.class,
          ClientConfig.class,
          CronTasksConfig.class})
@EnableCaching(order = 105)
@EnableWebMvc
public class WebConfig
        extends WebMvcConfigurerAdapter
{

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Inject
    @Named("customAntPathMatcher")
    CustomAntPathMatcher antPathMatcher;

    @Inject
    ObjectMapper objectMapper;

    public WebConfig()
    {
        logger.debug("Initialized web configuration.");
    }

    @Bean
    public HeaderMappingFilter headerMappingFilter()
    {
        return new HeaderMappingFilter();
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
        converters.add(marshallingMessageConverter());
        converters.add(new ResourceHttpMessageConverter());
    }

    @Bean
    public MarshallingHttpMessageConverter marshallingMessageConverter()
    {
        MarshallingHttpMessageConverter converter = new MarshallingHttpMessageConverter();
        converter.setMarshaller(marshaller());
        converter.setUnmarshaller(marshaller());
        return converter;
    }

    @Bean
    public Jaxb2Marshaller marshaller()
    {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("com.carlspring.strongbox.controllers",
                                     "org.carlspring.strongbox.artifact.coordinates",
                                     "org.carlspring.strongbox.authentication.registry",
                                     "org.carlspring.strongbox.authentication.support",
                                     "org.carlspring.strongbox.cron.domain",
                                     "org.carlspring.strongbox.configuration",
                                     "org.carlspring.strongbox.controllers",
                                     //TODO: resolve @XmlRootElement(name = "repository") conflict with  org.carlspring.strongbox.storage.repository.Repository
                                     //"org.carlspring.strongbox.providers.layout.p2",
                                     "org.carlspring.strongbox.storage",
                                     "org.carlspring.strongbox.storage.indexing",
                                     "org.carlspring.strongbox.storage.repository",
                                     "org.carlspring.strongbox.storage.repository.aws",
                                     "org.carlspring.strongbox.storage.repository.gcs",
                                     "org.carlspring.strongbox.storage.routing",
                                     "org.carlspring.strongbox.users.security",
                                     "org.carlspring.strongbox.xml");
        Map<String, Object> props = new HashMap<>();
        props.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setMarshallerProperties(props);
        return marshaller;
    }

    @Bean
    public MappingJackson2HttpMessageConverter jackson2Converter()
    {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer)
    {
        configurer.setUseSuffixPatternMatch(true)
                  .setUseTrailingSlashMatch(false)
                  .setUseRegisteredSuffixPatternMatch(true)
                  .setPathMatcher(antPathMatcher);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        registry
                .addResourceHandler("/docs/**")
                .addResourceLocations("/docs/")
                .setCachePeriod(3600);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry)
    {
        registry.addMapping("/**")
                .allowedMethods("*")
                .allowedOrigins("*");
    }
}
