package org.carlspring.strongbox.filters;

import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionService;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author mtodorov
 */
public class ArtifactResolutionServiceFilter
        implements javax.servlet.Filter
{

    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException
    {
        try
        {
            ArtifactResolutionService.getInstance().initialize();
        }
        catch (Exception e)
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
