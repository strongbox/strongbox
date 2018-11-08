package org.carlspring.strongbox.authentication.api.impl.xml;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.carlspring.strongbox.security.exceptions.ExpiredTokenException;
import org.carlspring.strongbox.security.exceptions.InvalidTokenException;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.carlspring.strongbox.users.userdetails.StrongboxUserDetailService;
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
public class JwtAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider
{

    @Inject
    @StrongboxUserDetailService.StrongboxUserDetailServiceQualifier
    private UserDetailsService userDetailsService;

    @Inject
    private SecurityTokenProvider securityTokenProvider;

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

        Map<String, String> claimMap = provideTokenClaims(userDetails);
        try
        {
            securityTokenProvider.verifyToken(token, authentication.getPrincipal().toString(), claimMap);
        }
        catch (ExpiredTokenException e)
        {
            throw new BadCredentialsException("expired");
        }
        catch (InvalidTokenException e)
        {
            throw new BadCredentialsException("invalid.token");
        }

    }

    protected Map<String, String> provideTokenClaims(UserDetails userDetails)
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
