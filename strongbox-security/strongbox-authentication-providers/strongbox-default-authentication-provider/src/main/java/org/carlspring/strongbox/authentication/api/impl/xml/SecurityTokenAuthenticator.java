package org.carlspring.strongbox.authentication.api.impl.xml;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.users.userdetails.SpringSecurityUser;
import org.carlspring.strongbox.users.userdetails.StrongboxUserDetailService;
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
public class SecurityTokenAuthenticator extends AbstractUserDetailsAuthenticationProvider
        implements Authenticator
{

    @Inject
    @StrongboxUserDetailService.StrongboxUserDetailServiceQualifier
    private UserDetailsService userDetailsService;

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
            logger.debug("Authentication failed: no credentials provided");

            throw new BadCredentialsException(
                    messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials",
                                        "Bad credentials"));
        }

        SecurityTokenAuthentication securityTokenAuthentication = (SecurityTokenAuthentication)authentication;
        String securityKey = securityTokenAuthentication.getCredentials().toString();

        if (!securityKey.equals(((SpringSecurityUser) userDetails).getSecurityKey()))
        {
            logger.debug("Authentication failed: User security key does not match stored value");

            throw new BadCredentialsException(messages.getMessage(
                                                                  "AbstractUserDetailsAuthenticationProvider.badCredentials",
                                                                  "Bad credentials"));
        }

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
            throw new InternalAuthenticationServiceException(
                    "UserDetailsService returned null, which is an interface contract violation");
        }
        return loadedUser;
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return SecurityTokenAuthentication.class.isAssignableFrom(authentication);
    }

}
