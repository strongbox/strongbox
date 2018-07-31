package org.carlspring.strongbox.security.authentication;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.security.authentication.suppliers.AuthenticationSuppliers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
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
            authentication = SecurityContextHolder.getContext().getAuthentication();
            logger.debug("Authentication not supplied by any authentication supplier, using [{}] context authentication.",
                         Optional.ofNullable(authentication).map(a -> a.getClass().getSimpleName()).orElse("empty"));
        }
        else
        {
            logger.debug("Supplied [{}] authentication.", authentication.getClass().getSimpleName());
        }

        authentication = provideAuthentication(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private Authentication provideAuthentication(Authentication authentication)
    {
        Authentication authResult = authentication;

        for (Authenticator authenticator : authenticatorsRegistry)
        {
            AuthenticationProvider authenticationProvider = authenticator.getAuthenticationProvider();
            String authenticationProviderName = authenticationProvider.getClass().getName();
            String authenticationName = authentication.getClass().getName();

            if (!authenticationProvider.supports(authentication.getClass()))
            {
                logger.debug("Authentication provider {} does not support {}", authenticationProviderName,
                             authenticationName);
                continue;
            }

            logger.debug("Try to authenticate {} with {}", authenticationName, authenticationProviderName);
            authResult = authenticationProvider.authenticate(authentication);
            logger.debug("Got success {} with {}", authenticationName, authenticationProviderName);

            break;
        }

        return authResult;
    }
}
