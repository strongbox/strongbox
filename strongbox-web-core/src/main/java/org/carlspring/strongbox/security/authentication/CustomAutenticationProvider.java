package org.carlspring.strongbox.security.authentication;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Alex Oreshkevich
 */
//@Component
public class CustomAutenticationProvider extends AbstractUserDetailsAuthenticationProvider
{

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken)
            throws AuthenticationException
    {

    }

    @Override
    protected UserDetails retrieveUser(String s,
                                       UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken)
            throws AuthenticationException
    {
        return null;
    }
}
