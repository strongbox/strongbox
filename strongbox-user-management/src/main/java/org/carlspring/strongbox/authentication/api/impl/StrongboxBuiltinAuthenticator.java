package org.carlspring.strongbox.authentication.api.impl;

import org.carlspring.strongbox.authentication.api.AuthenticationSupplier;
import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.api.impl.userdetails.StrongboxUserDetailService;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class StrongboxBuiltinAuthenticator
        implements Authenticator
{

    @Inject
    @StrongboxUserDetailService.StrongboxUserDetailServiceQ
    // in the future, there might be more than one UserDetailsService
    private UserDetailsService userDetailsService;

    @Nonnull
    @Override
    public AuthenticationProvider getAuthenticationProvider()
    {
        final DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        return authenticationProvider;
    }

    @Nonnull
    @Override
    public AuthenticationSupplier getAuthenticationSupplier()
    {
        return new StrongboxBuiltinAuthenticationSupplier();
    }
}
