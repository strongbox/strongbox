package org.carlspring.strongbox.authentication.api;

import javax.annotation.Nonnull;

import org.springframework.security.authentication.AuthenticationProvider;

/**
 * Main authentication API contract.
 *
 * @author Przemyslaw Fusik
 */
public interface Authenticator
{

    @Nonnull
    default String getName()
    {
        return getClass().getName();
    }

    @Nonnull
    AuthenticationProvider getAuthenticationProvider();

}
