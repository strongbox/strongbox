package org.carlspring.strongbox.security.authentication.suppliers;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

/**
 * @author Przemyslaw Fusik
 */
public class AuthenticationSuppliers
        implements AuthenticationSupplier
{

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationSuppliers.class);

    private final List<AuthenticationSupplier> suppliers;

    public AuthenticationSuppliers(List<AuthenticationSupplier> suppliers)
    {
        this.suppliers = suppliers;
    }

    @CheckForNull
    @Override
    public Authentication supply(@Nonnull HttpServletRequest request)
    {
        Authentication authentication = null;
        for (final AuthenticationSupplier supplier : suppliers)
        {
            final String supplierName = supplier.getClass()
                                                .getName();

            if (!supplier.supports(request))
            {
                logger.debug("Supplier {} does not support this request [method: {}] [URI: {}] [ContentType {}]",
                             supplierName, request.getMethod(), request.getRequestURI(), request.getContentType());
                continue;
            }

            logger.debug("Authentication supplier attempt using {}", supplierName);
            authentication = supplier.supply(request);

            if (authentication == null)
            {
                logger.debug("Unable to get an authentication instance using {}", supplierName);
                continue;
            }

            logger.debug("Authentication supplied by {}", supplierName);
            break;
        }
        return authentication;
    }
}
