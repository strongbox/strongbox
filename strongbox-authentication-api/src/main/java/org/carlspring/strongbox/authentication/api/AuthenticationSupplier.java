package org.carlspring.strongbox.authentication.api;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;

/**
 * @author Przemyslaw Fusik
 */
@FunctionalInterface
public interface AuthenticationSupplier
{

    /**
     * Attempts to supply the {@link Authentication} object from the currently served HTTP request.
     */
    @CheckForNull
    Authentication supply(@Nonnull HttpServletRequest request);

}
