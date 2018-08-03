package org.carlspring.strongbox.config;

import org.carlspring.strongbox.web.DirectoryTraversalFilter;
import org.carlspring.strongbox.web.HeaderMappingFilter;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.EnumSet;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.filter.RequestContextFilter;

@Configuration
public class ServletConfig
{

    private static FilterRegistrationBean registerFilter(String filterName,
                                                         Filter filter)
    {
        FilterRegistrationBean registration = new FilterRegistrationBean();

        EnumSet<DispatcherType> dispatcherTypes = getSecurityDispatcherTypes();

        registration.setFilter(filter);
        registration.setName(filterName);
        registration.setAsyncSupported(true);
        registration.setDispatcherTypes(dispatcherTypes);
        registration.addUrlPatterns("/*");

        return registration;
    }

    private static EnumSet<DispatcherType> getSecurityDispatcherTypes()
    {
        return EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR, DispatcherType.ASYNC);
    }

    @Bean
    RequestContextListener requestContextListener()
    {
        return new RequestContextListener();
    }

    @Bean
    FilterRegistrationBean commonsRequestLoggingFilter()
    {
        return registerFilter(CommonsRequestLoggingFilter.class.getSimpleName(),
                              new DelegatingFilterProxy("commonsRequestLoggingFilter"));
    }

    @Bean
    FilterRegistrationBean directoryTraversalFilter()
    {
        return registerFilter(DirectoryTraversalFilter.class.getSimpleName(), new DirectoryTraversalFilter());
    }

    @Bean
    FilterRegistrationBean headerMappingFilter()
    {
        return registerFilter(HeaderMappingFilter.class.getSimpleName(),
                              new DelegatingFilterProxy("headerMappingFilter"));
    }

    @Bean
    FilterRegistrationBean requestContextFilter()
    {
        return registerFilter(RequestContextFilter.class.getSimpleName(), new RequestContextFilter());
    }

}
