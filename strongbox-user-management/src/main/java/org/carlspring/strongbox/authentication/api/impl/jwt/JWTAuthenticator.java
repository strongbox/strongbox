package org.carlspring.strongbox.authentication.api.impl.jwt;

import org.carlspring.strongbox.authentication.api.AuthenticationSupplier;
import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.api.impl.userdetails.StrongboxUserDetailService;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class JWTAuthenticator
        implements Authenticator
{

    @Inject
    @StrongboxUserDetailService.StrongboxUserDetailServiceQualifier
    private UserDetailsService userDetailsService;

    @Inject
    private SecurityTokenProvider securityTokenProvider;

    @Nonnull
    @Override
    public AuthenticationProvider getAuthenticationProvider()
    {
        return new JWTAuthenticationProvider(userDetailsService, securityTokenProvider);
    }

    @Nonnull
    @Override
    public AuthenticationSupplier getAuthenticationSupplier()
    {
        return new JWTAuthenticationSupplier();
    }
}
