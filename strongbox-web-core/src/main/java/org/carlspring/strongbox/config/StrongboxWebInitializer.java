package org.carlspring.strongbox.config;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.carlspring.strongbox.web.HeaderMappingFilter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

public class StrongboxWebInitializer extends AbstractDispatcherServletInitializer
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
        return new String[] { "/*" };
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

        registerFilter(servletContext, true, HeaderMappingFilter.class.getSimpleName(), new HeaderMappingFilter());

        String filterName = "springSecurityFilterChain";
        DelegatingFilterProxy springSecurityFilterChain = new DelegatingFilterProxy(
                filterName);
        registerFilter(servletContext, false, filterName, springSecurityFilterChain);
    }

    private final void registerFilter(ServletContext servletContext,
                                      boolean insertBeforeOtherFilters,
                                      String filterName,
                                      Filter filter)
    {
        Dynamic registration = servletContext.addFilter(filterName, filter);
        registration.setAsyncSupported(true);
        EnumSet<DispatcherType> dispatcherTypes = getSecurityDispatcherTypes();
        registration.addMappingForUrlPatterns(dispatcherTypes, !insertBeforeOtherFilters,
                                              "/*");
    }

    protected EnumSet<DispatcherType> getSecurityDispatcherTypes()
    {
        return EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR,
                          DispatcherType.ASYNC);
    }

}
