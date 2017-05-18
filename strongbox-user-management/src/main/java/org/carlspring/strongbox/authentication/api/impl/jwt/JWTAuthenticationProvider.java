package org.carlspring.strongbox.authentication.api.impl.jwt;

import org.carlspring.strongbox.security.exceptions.SecurityTokenException;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class JWTAuthenticationProvider
        implements AuthenticationProvider
{

    private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationProvider.class);

    private final DaoAuthenticationProvider delegate;

    private final UserDetailsService userDetailsService;

    private final SecurityTokenProvider securityTokenProvider;

    public JWTAuthenticationProvider(UserDetailsService userDetailsService,
                                     SecurityTokenProvider securityTokenProvider)
    {
        this.userDetailsService = userDetailsService;
        this.securityTokenProvider = securityTokenProvider;

        delegate = new DaoAuthenticationProvider();
        delegate.setUserDetailsService(userDetailsService);
    }

    @Override
    public Authentication authenticate(Authentication authentication)
    {

        JWTAuthentication jwtAuthentication = (JWTAuthentication) authentication;
        String token = jwtAuthentication.getToken();
        String userName = securityTokenProvider.getSubject(token);

        UserDetails user = userDetailsService.loadUserByUsername(userName);

        Map<String, String> claimMap = new HashMap<>();
        claimMap.put("credentials", user.getPassword());
        try
        {
            securityTokenProvider.verifyToken(token, userName, claimMap);
        }
        catch (SecurityTokenException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof SecurityTokenException)
            {
                throw new JwtAuthenticationException(String.format("Invalid JWT: value-[%s]", token), e);
            }
            else
            {
                throw new BadCredentialsException(String.format("Credentials don't match: user-[%s] ", userName), e);
            }
        }

        logger.debug("Token verified. Delegating authentication to {}", delegate);
        return delegate.authenticate(new UsernamePasswordAuthenticationToken(userName, user.getPassword()));
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return JWTAuthentication.class.isAssignableFrom(authentication);
    }

    public static class JwtAuthenticationException
            extends AuthenticationException
    {

        public JwtAuthenticationException(String msg,
                                          Throwable t)
        {
            super(msg, t);
        }

        public JwtAuthenticationException(String msg)
        {
            super(msg);
        }

    }
}
