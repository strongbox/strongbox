package org.carlspring.strongbox.authentication.api.impl.xml;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.security.exceptions.SecurityTokenException;
import org.carlspring.strongbox.security.exceptions.SecurityTokenExpiredException;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.carlspring.strongbox.users.userdetails.StrongboxUserDetailService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author Sergey Bespalov
 *
 */
public class JwtAuthenticator extends AbstractUserDetailsAuthenticationProvider implements Authenticator
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
        return this;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
        throws AuthenticationException
    {
        if (authentication.getCredentials() == null)
        {
            throw new BadCredentialsException("No credentials provided.");
        }

        String token = authentication.getCredentials().toString();

        Map<String, String> claimMap = provideTargetClaims(userDetails);
        try
        {
            securityTokenProvider.verifyToken(token, authentication.getPrincipal().toString(), claimMap);
        }
        catch (SecurityTokenExpiredException e)
        {
            throw new BadCredentialsException("expired");
        }
        catch (SecurityTokenException e)
        {
            throw new BadCredentialsException("invalid.token");
        }

    }

    protected Map<String, String> provideTargetClaims(UserDetails userDetails)
    {
        return Collections.emptyMap();
    }

    @Override
    protected UserDetails retrieveUser(String username,
                                       UsernamePasswordAuthenticationToken authentication)
        throws AuthenticationException
    {
        UserDetails loadedUser;
        try
        {
            loadedUser = userDetailsService.loadUserByUsername(username);
        }
        catch (UsernameNotFoundException notFound)
        {
            throw notFound;
        }
        catch (Exception repositoryProblem)
        {
            throw new InternalAuthenticationServiceException(
                    repositoryProblem.getMessage(), repositoryProblem);
        }

        if (loadedUser == null)
        {
            throw new UsernameNotFoundException(String.format("User [%s] not found.", username));
        }
        return loadedUser;
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return JwtAuthentication.class.isAssignableFrom(authentication);
    }

}
