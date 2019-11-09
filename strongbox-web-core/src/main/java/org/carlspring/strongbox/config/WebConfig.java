package org.carlspring.strongbox.config;

import org.carlspring.strongbox.configuration.StrongboxSecurityConfig;
import org.carlspring.strongbox.converters.RoleFormToRoleConverter;
import org.carlspring.strongbox.converters.RoleListFormToRoleListConverter;
import org.carlspring.strongbox.converters.configuration.ProxyConfigurationFormConverter;
import org.carlspring.strongbox.converters.configuration.RemoteRepositoryFormConverter;
import org.carlspring.strongbox.converters.configuration.RepositoryFormConverter;
import org.carlspring.strongbox.converters.configuration.StorageFormConverter;
import org.carlspring.strongbox.converters.cron.CronTaskConfigurationFormToCronTaskConfigurationDtoConverter;
import org.carlspring.strongbox.converters.storage.routing.RoutingRuleFormToMutableConverter;
import org.carlspring.strongbox.converters.users.AccessModelFormToUserAccessModelDtoConverter;
import org.carlspring.strongbox.converters.users.UserFormToUserDtoConverter;
import org.carlspring.strongbox.cron.config.CronTasksConfig;
import org.carlspring.strongbox.interceptors.MavenArtifactRequestInterceptor;
import org.carlspring.strongbox.mapper.WebObjectMapperSubtypes;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.utils.CustomAntPathMatcher;
import org.carlspring.strongbox.web.CustomRequestMappingHandlerMapping;
import org.carlspring.strongbox.web.DirectoryTraversalFilter;
import org.carlspring.strongbox.web.RepositoryMethodArgumentResolver;
import org.carlspring.strongbox.yaml.YAMLMapperFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

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
import org.springframework.http.MediaType;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.resource.GzipResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import static org.carlspring.strongbox.net.MediaType.APPLICATION_YAML_VALUE;
import static org.carlspring.strongbox.net.MediaType.APPLICATION_YML_VALUE;

@Configuration
@ComponentScan({ "com.carlspring.strongbox.controllers",
                 "org.carlspring.strongbox.controllers",
                 "org.carlspring.strongbox.validation",
                 "org.carlspring.strongbox.web",
                 "org.carlspring.strongbox.mapper",
                 "org.carlspring.strongbox.utils",
                 "org.carlspring.strongbox.actuator" })
@Import({ CommonConfig.class,
          StrongboxSecurityConfig.class,
          StorageApiConfig.class,
          EventsConfig.class,
          StorageCoreConfig.class,
          UsersConfig.class,
          SecurityConfig.class,
          ClientConfig.class,
          CronTasksConfig.class,
          SwaggerConfig.class })
@EnableCaching(order = 105)
public class WebConfig
        extends WebMvcConfigurationSupport
{

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Inject
    @Named("customAntPathMatcher")
    private CustomAntPathMatcher antPathMatcher;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private YAMLMapperFactory yamlMapperFactory;

    WebConfig()
    {
        logger.debug("Initialized web configuration.");
    }

    @Bean
    RequestContextListener requestContextListener()
    {
        return new RequestContextListener();
    }

    @Bean
    RequestContextFilter requestContextFilter()
    {
        return new RequestContextFilter();
    }

    @Bean
    CommonsRequestLoggingFilter commonsRequestLoggingFilter()
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
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping()
    {
        return new CustomRequestMappingHandlerMapping();
    }

    @Bean
    DirectoryTraversalFilter directoryTraversalFilter()
    {
        return new DirectoryTraversalFilter();
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
        converters.add(yamlConverter());
        converters.add(new ResourceHttpMessageConverter());
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer)
    {
        configurer.favorPathExtension(false);
    }

    // TODO consider using the same MappingJackson2HttpMessageConverter for yaml and json !
    @Bean
    public MappingJackson2HttpMessageConverter yamlConverter()
    {
        MappingJackson2HttpMessageConverter yamlConverter = new MappingJackson2HttpMessageConverter(
                yamlMapperFactory.create(WebObjectMapperSubtypes.INSTANCE.subtypes()));
        yamlConverter.setSupportedMediaTypes(
                Arrays.asList(MediaType.valueOf(APPLICATION_YML_VALUE), MediaType.valueOf(APPLICATION_YAML_VALUE)));
        return yamlConverter;
    }

    @Bean
    public MappingJackson2HttpMessageConverter jackson2Converter()
    {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

    @Bean
    public Validator localValidatorFactoryBean()
    {
        return new LocalValidatorFactoryBean();
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer)
    {
        configurer.setUseRegisteredSuffixPatternMatch(false)
                  .setUseSuffixPatternMatch(false)
                  .setUseTrailingSlashMatch(true)
                  .setPathMatcher(antPathMatcher);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        registry.setOrder(-1);

        registry.addResourceHandler("/docs/**")
                .addResourceLocations("classpath:/META-INF/resources/docs/")
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

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

    }

    @Override
    public void addFormatters(FormatterRegistry registry)
    {
        registry.addConverter(new RoleFormToRoleConverter());
        registry.addConverter(new RoleListFormToRoleListConverter());
        registry.addConverter(UserFormToUserDtoConverter.INSTANCE);
        registry.addConverter(AccessModelFormToUserAccessModelDtoConverter.INSTANCE);
        registry.addConverter(ProxyConfigurationFormConverter.INSTANCE);
        registry.addConverter(new RoutingRuleFormToMutableConverter());
        registry.addConverter(StorageFormConverter.INSTANCE);
        registry.addConverter(RepositoryFormConverter.INSTANCE);
        registry.addConverter(RemoteRepositoryFormConverter.INSTANCE);
        registry.addConverter(CronTaskConfigurationFormToCronTaskConfigurationDtoConverter.INSTANCE);
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

    @Bean
    MavenArtifactRequestInterceptor mavenArtifactRequestInterceptor(RepositoryPathResolver repositoryPathResolver)
    {
        return new MavenArtifactRequestInterceptor(repositoryPathResolver);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers)
    {
        argumentResolvers.add(repositoryMethodArgumentResolver());
    }

    @Bean
    public RepositoryMethodArgumentResolver repositoryMethodArgumentResolver()
    {
        return new RepositoryMethodArgumentResolver();
    }
}
