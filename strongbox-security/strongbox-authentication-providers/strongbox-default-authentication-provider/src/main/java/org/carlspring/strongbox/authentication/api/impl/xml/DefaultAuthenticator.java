package org.carlspring.strongbox.authentication.api.impl.xml;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.users.userdetails.StrongboxUserDetailService;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Przemyslaw Fusik
 */
public class DefaultAuthenticator
        implements Authenticator
{

    @Inject
    @StrongboxUserDetailService.StrongboxUserDetailServiceQualifier
    private UserDetailsService userDetailsService;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Nonnull
    @Override
    public AuthenticationProvider getAuthenticationProvider()
    {
        final DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(){

            @Override
            public boolean supports(Class<?> authentication)
            {
                return UsernamePasswordAuthenticationToken.class == authentication;
            }
            
        };
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        
        return authenticationProvider;
    }
}
