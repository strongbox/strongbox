package org.carlspring.strongbox.config;

import org.carlspring.strongbox.configuration.StrongboxSecurityConfig;
import org.carlspring.strongbox.converters.PrivilegeListFormToPrivilegeListConverter;
import org.carlspring.strongbox.converters.RoleFormToRoleConverter;
import org.carlspring.strongbox.converters.RoleListFormToRoleListConverter;
import org.carlspring.strongbox.converters.configuration.ProxyConfigurationFormConverter;
import org.carlspring.strongbox.converters.configuration.RemoteRepositoryFormConverter;
import org.carlspring.strongbox.converters.configuration.RepositoryFormConverter;
import org.carlspring.strongbox.converters.configuration.StorageFormConverter;
import org.carlspring.strongbox.converters.storage.routing.RoutingRuleFormToRoutingRuleConverter;
import org.carlspring.strongbox.converters.storage.routing.RuleSetFormToRuleSetConverter;
import org.carlspring.strongbox.converters.users.AccessModelFormToUserAccessModelDtoConverter;
import org.carlspring.strongbox.converters.users.UserFormToUserDtoConverter;
import org.carlspring.strongbox.cron.config.CronTasksConfig;
import org.carlspring.strongbox.utils.CustomAntPathMatcher;
import org.carlspring.strongbox.web.HeaderMappingFilter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.Marshaller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jtwig.spring.boot.config.JtwigViewResolverConfigurer;
import org.jtwig.web.servlet.JtwigRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.GzipResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@ComponentScan({ "com.carlspring.strongbox.controllers",
                 "org.carlspring.strongbox.controllers",
                 "org.carlspring.strongbox.validation",
                 "org.carlspring.strongbox.web",
                 "org.carlspring.strongbox.mapper",
                 "org.carlspring.strongbox.utils",
                 "org.carlspring.logging" })
@Import({ CommonConfig.class,
          StrongboxSecurityConfig.class,
          StorageApiConfig.class,
          EventsConfig.class,
          Maven2LayoutProviderConfig.class,
          NugetLayoutProviderConfig.class,
          NpmLayoutProviderConfig.class,
          RawLayoutProviderConfig.class,
          StorageCoreConfig.class,
          UsersConfig.class,
          SecurityConfig.class,
          ClientConfig.class,
          CronTasksConfig.class })
@EnableCaching(order = 105)
@EnableWebMvc
public class WebConfig
        implements WebMvcConfigurer
{

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Inject
    @Named("customAntPathMatcher")
    private CustomAntPathMatcher antPathMatcher;

    @Inject
    private ObjectMapper objectMapper;

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
                                     "org.carlspring.strongbox.users.dto",
                                     "org.carlspring.strongbox.authorization.dto",
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
                                     "org.carlspring.strongbox.xml",
                                     "org.carlspring.strongbox.forms");
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

    @Bean
    public Validator localValidatorFactoryBean()
    {
        return new LocalValidatorFactoryBean();
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer)
    {
        configurer.setUseSuffixPatternMatch(true)
                  .setUseTrailingSlashMatch(true)
                  .setUseRegisteredSuffixPatternMatch(true)
                  .setPathMatcher(antPathMatcher);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        registry.addResourceHandler("/docs/**")
                .addResourceLocations("/docs/")
                .setCachePeriod(3600);

        registry.addResourceHandler("*.html")
                .addResourceLocations("classpath:/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/static/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new GzipResourceResolver())
                .addResolver(new PathResourceResolver());
    }

    @Bean
    public CommonsRequestLoggingFilter commonsRequestLoggingFilter()
    {
        CommonsRequestLoggingFilter result = new CommonsRequestLoggingFilter()
        {

            @Override
            protected String createMessage(HttpServletRequest request,
                                           String prefix,
                                           String suffix)
            {
                return super.createMessage(request, String.format("%smethod=%s;", prefix, request.getMethod()), suffix);
            }

        };
        result.setIncludeQueryString(true);
        result.setIncludeHeaders(true);
        result.setIncludeClientInfo(true);
        return result;
    }

    @Override
    public void addFormatters(FormatterRegistry registry)
    {
        registry.addConverter(new RoleFormToRoleConverter());
        registry.addConverter(new RoleListFormToRoleListConverter());
        registry.addConverter(new PrivilegeListFormToPrivilegeListConverter());
        registry.addConverter(UserFormToUserDtoConverter.INSTANCE);
        registry.addConverter(AccessModelFormToUserAccessModelDtoConverter.INSTANCE);
        registry.addConverter(ProxyConfigurationFormConverter.INSTANCE);
        registry.addConverter(new RuleSetFormToRuleSetConverter());
        registry.addConverter(new RoutingRuleFormToRoutingRuleConverter());
        registry.addConverter(StorageFormConverter.INSTANCE);
        registry.addConverter(RepositoryFormConverter.INSTANCE);
        registry.addConverter(RemoteRepositoryFormConverter.INSTANCE);
    }

    @Bean
    JtwigViewResolverConfigurer jtwigViewResolverConfigurer()
    {
        return jtwigViewResolver ->
        {
            jtwigViewResolver.setRenderer(JtwigRenderer.defaultRenderer());
            jtwigViewResolver.setPrefix("classpath:/views/");
            jtwigViewResolver.setSuffix(".twig.html");
            jtwigViewResolver.setViewNames("directoryListing");
            jtwigViewResolver.setOrder(0);
        };
    }


    @Bean
    InternalResourceViewResolver internalResourceViewResolver()
    {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(InternalResourceView.class);
        viewResolver.setViewNames("*.html");
        viewResolver.setOrder(1);

        return viewResolver;
    }
}
