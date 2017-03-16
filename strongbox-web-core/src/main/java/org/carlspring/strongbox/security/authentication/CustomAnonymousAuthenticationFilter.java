package org.carlspring.strongbox.security.authentication;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/**
 * Custom anonymous authentication filter that allows us to change the anonymous authorities at runtime.
 *
 * @author Alex Oreshkevich
 * @see {@linkplain https://dev.carlspring.org/youtrack/issue/SB-606}
 */
public class CustomAnonymousAuthenticationFilter
        extends AnonymousAuthenticationFilter
{

    private boolean contextAutoCreationEnabled;

    public CustomAnonymousAuthenticationFilter(String key,
                                               Object principal,
                                               List<GrantedAuthority> authorities)
    {
        super(key, principal, authorities);
        contextAutoCreationEnabled = true;
    }

    public void doFilter(ServletRequest req,
                         ServletResponse res,
                         FilterChain chain)
            throws IOException, ServletException
    {
        if (contextAutoCreationEnabled)
        {
            super.doFilter(req, res, chain);
        }
        else
        {
            logger.debug("Auto creation of SecurityContext was disabled. SecurityContextHolder was NOT modified.");

            if (SecurityContextHolder.getContext()
                                     .getAuthentication() == null)
            {
                HttpServletResponse response = (HttpServletResponse) res;
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Authentication is null");
            }
            else
            {
                logger.debug(
                        "Authenticated under " + SecurityContextHolder.getContext()
                                                                      .getAuthentication()
                                                                      .getPrincipal());
                chain.doFilter(req, res);
            }
        }
    }

    public boolean isContextAutoCreationEnabled()
    {
        return contextAutoCreationEnabled;
    }

    public void setContextAutoCreationEnabled(boolean contextAutoCreationEnabled)
    {
        this.contextAutoCreationEnabled = contextAutoCreationEnabled;
    }
}
