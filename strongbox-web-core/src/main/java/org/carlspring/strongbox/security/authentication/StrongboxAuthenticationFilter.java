package org.carlspring.strongbox.security.authentication;

import org.carlspring.strongbox.authentication.api.AuthenticationSupplier;
import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public StrongboxAuthenticationFilter(AuthenticatorsRegistry authenticatorsRegistry)
    {
        super();
        this.authenticatorsRegistry = authenticatorsRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException,
                   IOException
    {
        Authentication result = null;

        for (final Authenticator authenticator : authenticatorsRegistry.getAuthenticators())
        {

            final AuthenticationSupplier authenticationSupplier = authenticator.getAuthenticationSupplier();

            logger.debug("Authentication supplier attempt using {}", authenticationSupplier.getClass()
                                                                                           .getName());
            final Authentication authentication = authenticationSupplier.supply(request);

            if (authentication == null)
            {
                logger.debug("Unable to get an authentication instance using {}", authenticationSupplier.getClass()
                                                                                                        .getName());
                continue;
            }

            final AuthenticationProvider authenticationProvider = authenticator.getAuthenticationProvider();
            logger.debug("Authentication provider attempt using {}", authenticationProvider.getClass()
                                                                                           .getName());

            try
            {
                result = authenticationProvider.authenticate(authentication);

                if (result != null)
                {
                    SecurityContextHolder.getContext()
                                         .setAuthentication(result);
                    logger.debug("Authentication success using {}", authenticationProvider.getClass()
                                                                                          .getName());
                    break;
                }
            }
            catch (AuthenticationException e)
            {
                logger.debug("Authentication request failed", e);
                continue;
            }
        }

        filterChain.doFilter(request, response);

    }
}
