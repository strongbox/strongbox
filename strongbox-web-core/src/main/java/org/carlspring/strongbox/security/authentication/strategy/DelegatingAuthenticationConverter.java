package org.carlspring.strongbox.security.authentication.strategy;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationConverter;

/**
 * @author Przemyslaw Fusik
 */
public class DelegatingAuthenticationConverter
        implements AuthenticationConverter
{

    private static final Logger logger = LoggerFactory.getLogger(DelegatingAuthenticationConverter.class);

    private final List<AuthenticationStrategy> strategies;

    public DelegatingAuthenticationConverter(List<AuthenticationStrategy> strategies)
    {
        this.strategies = strategies;
    }

    @CheckForNull
    @Override
    public Authentication convert(@Nonnull HttpServletRequest request)
    {
        if (strategies == null || strategies.isEmpty())
        {
            logger.debug("There was no [{}] provided.", AuthenticationStrategy.class);

            return null;
        }

        AuthenticationException lastException = null;
        for (final AuthenticationStrategy strategy : strategies)
        {
            final String supplierName = strategy.getClass()
                                                .getName();

            if (!strategy.supports(request))
            {
                logger.debug("Supplier {} does not support this request [method: {}] [URI: {}] [ContentType {}]",
                             supplierName, request.getMethod(), request.getRequestURI(), request.getContentType());
                continue;
            }

            logger.debug("Authentication supplier attempt using {}", supplierName);
            Authentication authentication;
            try
            {
                authentication = strategy.convert(request);
            }
            catch (AuthenticationException e)
            {
                lastException = e;
                continue;
            }

            if (authentication != null)
            {
                logger.debug("Authentication supplied by {}", supplierName);

                return authentication;
            }
        }
        if (lastException != null)
        {
            throw lastException;
        }
        
        return null;
    }
}
