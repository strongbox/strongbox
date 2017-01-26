package org.carlspring.strongbox.security.authentication;

import org.carlspring.strongbox.security.exceptions.SecurityTokenException;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class JWtAuthenticationProvider
        implements AuthenticationProvider
{

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private SecurityTokenProvider securityTokenProvider;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException
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
                throw new BadCredentialsException(
                                                         String.format(
                                                                 String.format("Credentials don't match: user-[%s] ",
                                                                               userName)), e);
            }
        }
        return authentication;
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
