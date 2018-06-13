package org.carlspring.strongbox.config;

import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.carlspring.strongbox.web.DirectoryTraversalFilter;
import org.carlspring.strongbox.web.HeaderMappingFilter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

public class StrongboxWebInitializer
        extends AbstractDispatcherServletInitializer
{

    @Override
    protected WebApplicationContext createServletApplicationContext()
    {
        XmlWebApplicationContext result = new XmlWebApplicationContext();
        result.setConfigLocation("classpath:META-INF/spring/strongbox-web-context.xml");
        return result;
    }

    @Override
    protected String[] getServletMappings()
    {
        return new String[]{ "/*" };
    }

    @Override
    protected WebApplicationContext createRootApplicationContext()
    {
        XmlWebApplicationContext result = new XmlWebApplicationContext();
        result.setConfigLocation("classpath:applicationContext.xml");

        return result;
    }

    public final void onStartup(ServletContext servletContext)
            throws ServletException
    {
        super.onStartup(servletContext);

        if (System.getProperty(ConnectionConfigOrientDB.PROPERTY_PROFILE) == null)
        {
            logger.info(String.format("OrientDB profile not set, will use [%s] profile as default",
                                      ConnectionConfigOrientDB.PROFILE_EMBEDDED));
            System.setProperty(ConnectionConfigOrientDB.PROPERTY_PROFILE, ConnectionConfigOrientDB.PROFILE_EMBEDDED);
        }
        
        servletContext.addListener(new RequestContextListener());
        
        registerFilter(servletContext, true, CommonsRequestLoggingFilter.class.getSimpleName(),
                       new DelegatingFilterProxy("commonsRequestLoggingFilter"));

        registerFilter(servletContext, false, DirectoryTraversalFilter.class.getSimpleName(),
                       new DirectoryTraversalFilter());

        DelegatingFilterProxy headerMappingFilterDelegate = new DelegatingFilterProxy("headerMappingFilter");
        registerFilter(servletContext, false, HeaderMappingFilter.class.getSimpleName(), headerMappingFilterDelegate);

        String filterName = "springSecurityFilterChain";
        DelegatingFilterProxy springSecurityFilterChain = new DelegatingFilterProxy(filterName);
        registerFilter(servletContext, false, filterName, springSecurityFilterChain);

        registerFilter(servletContext, false, RequestContextFilter.class.getSimpleName(), new RequestContextFilter());
    }

    private final void registerFilter(ServletContext servletContext,
                                      boolean insertBeforeOtherFilters,
                                      String filterName,
                                      Filter filter)
    {
        EnumSet<DispatcherType> dispatcherTypes = getSecurityDispatcherTypes();

        Dynamic registration = servletContext.addFilter(filterName, filter);
        registration.setAsyncSupported(true);
        registration.addMappingForUrlPatterns(dispatcherTypes, !insertBeforeOtherFilters, "/*");
    }

    protected EnumSet<DispatcherType> getSecurityDispatcherTypes()
    {
        return EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR, DispatcherType.ASYNC);
    }

}
