package org.carlspring.strongbox.security.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * @author Alex Oreshkevich
 */
@Component
public class CustomAuthenticationProvider
        implements AuthenticationProvider
{

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationProvider.class);

    @Autowired
    UserDetailsService userService;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException
    {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        logger.debug("[authenticate] user " + username);

        UserDetails user = userService.loadUserByUsername(username);
        if (!password.equals(user.getPassword()))
        {
            throw new BadCredentialsException("Wrong password.");
        }

        return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> aClass)
    {
        return true;
    }
}
