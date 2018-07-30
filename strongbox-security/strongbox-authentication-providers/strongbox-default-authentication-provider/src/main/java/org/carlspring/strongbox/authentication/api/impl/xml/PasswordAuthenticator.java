package org.carlspring.strongbox.authentication.api.impl.xml;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.users.userdetails.StrongboxUserDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Przemyslaw Fusik
 * @author sbespalov
 */
public class PasswordAuthenticator extends DaoAuthenticationProvider
        implements Authenticator
{

    private static final Logger logger = LoggerFactory.getLogger(PasswordAuthenticator.class);

    @Inject
    private AuthenticationCache authenticationCache;

    @Nonnull
    @Override
    public AuthenticationProvider getAuthenticationProvider()
    {
        return this;
    }

    @Override
    @Inject
    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        super.setPasswordEncoder(passwordEncoder);
    }

    @Override
    @Inject
    @StrongboxUserDetailService.StrongboxUserDetailServiceQualifier
    public void setUserDetailsService(UserDetailsService userDetailsService)
    {
        super.setUserDetailsService(userDetailsService);
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return PasswordAuthentication.class.isAssignableFrom(authentication);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
        throws AuthenticationException
    {
        UsernamePasswordAuthenticationToken cachedAuthentication = authenticationCache.getAuthenticationToken(userDetails.getUsername());

        if (Optional.ofNullable(cachedAuthentication)
                    .filter(c -> authenticationCache.matches(authentication.getCredentials().toString(),
                                                             c.getCredentials().toString()))
                    .isPresent())

        {
            logger.debug(String.format("Found cached authentication for [%s]",
                                       userDetails.getUsername()));
            return;
        }

        super.additionalAuthenticationChecks(userDetails, authentication);

        authenticationCache.putAuthenticationToken(authentication);
    }

}
