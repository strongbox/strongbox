package org.carlspring.strongbox.security.authentication.suppliers;

import org.carlspring.strongbox.authentication.api.impl.userdetails.StrongboxUserDetailService;
import org.carlspring.strongbox.security.exceptions.SecurityTokenException;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
class JWTAuthenticationSupplier
        implements AuthenticationSupplier
{

    private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationSupplier.class);

    private static final Pattern BEARER_PATTERN = Pattern.compile("Bearer (.*)");

    @Inject
    @StrongboxUserDetailService.StrongboxUserDetailServiceQualifier
    private UserDetailsService userDetailsService;

    @Inject
    private SecurityTokenProvider securityTokenProvider;

    @CheckForNull
    @Override
    public Authentication supply(@Nonnull HttpServletRequest request)
    {
        final String tokenHeader = request.getHeader("Authorization");
        if (tokenHeader == null)
        {
            return null;
        }

        final Matcher matcher = BEARER_PATTERN.matcher(tokenHeader);
        if (!matcher.matches())
        {
            return null;
        }

        final String token = matcher.group(1);

        logger.debug("Bearer Authorization header found with token {}", token);

        String userName = securityTokenProvider.getSubject(token);
        UserDetails user = userDetailsService.loadUserByUsername(userName);

        if (user == null)
        {
            logger.debug("User not found for token {}", token);
            return null;
        }

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

        logger.debug("Token verified.");
        return new UsernamePasswordAuthenticationToken(userName, user.getPassword());
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
