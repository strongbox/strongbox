package org.carlspring.strongbox.security.authentication;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.security.authentication.suppliers.AuthenticationSuppliers;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author Przemyslaw Fusik
 */
public class StrongboxAuthenticationFilter
        extends OncePerRequestFilter
{

    private static final Logger logger = LoggerFactory.getLogger(StrongboxAuthenticationFilter.class);

    private final AuthenticatorsRegistry authenticatorsRegistry;

    private final AuthenticationSuppliers authenticationSuppliers;

    public StrongboxAuthenticationFilter(AuthenticationSuppliers authenticationSuppliers,
                                         AuthenticatorsRegistry authenticatorsRegistry)
    {
        super();
        this.authenticationSuppliers = authenticationSuppliers;
        this.authenticatorsRegistry = authenticatorsRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException,
                   IOException
    {
        Authentication authentication = authenticationSuppliers.supply(request);

        if (authentication == null)
        {
            logger.debug(
                    "Authentication not supplied by any authentication supplier. Skipping authentication providing.");
        }
        else
        {
            provideAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private void provideAuthentication(Authentication authentication)
    {
        for (final Authenticator authenticator : authenticatorsRegistry)
        {
            final AuthenticationProvider authenticationProvider = authenticator.getAuthenticationProvider();

            if (!authenticationProvider.supports(authentication.getClass()))
            {
                logger.debug("Authentication provider {} does not support {}",
                             authenticationProvider.getClass().getName(), authentication.getClass().getName());
                continue;
            }

            try
            {
                authentication = authenticationProvider.authenticate(authentication);

                if (authentication != null)
                {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Authentication success using {}", authenticationProvider.getClass().getName());
                    break;
                }
            }
            catch (AuthenticationException e)
            {
                logger.debug("Authentication request failed", e);
                continue;
            }
        }
    }
}
