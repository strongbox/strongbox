package org.carlspring.strongbox.web;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author Przemyslaw Fusik
 */
public class DirectoryTraversalFilter
        extends OncePerRequestFilter
{

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException
    {
        String requestedUrl = request.getRequestURL().toString();
        String normalizedUrl = StringUtils.cleanPath(requestedUrl);
        if (!requestedUrl.equals(normalizedUrl))
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "Invalid path provided. Please make sure there are no sequences like \"path/..\" in your request url.");
            return;
        }
        filterChain.doFilter(request, response);
    }

}
