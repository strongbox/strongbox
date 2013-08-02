package org.carlspring.repositoryunit.filters;

import org.carlspring.repositoryunit.configuration.ConfigurationManager;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author mtodorov
 */
public class LoadConfigurationFilter
        implements javax.servlet.Filter
{

    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException
    {
        try
        {
            ConfigurationManager.initialize();
        }
        catch (IOException e)
        {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain)
            throws IOException, ServletException
    {

    }

    @Override
    public void destroy()
    {

    }

}