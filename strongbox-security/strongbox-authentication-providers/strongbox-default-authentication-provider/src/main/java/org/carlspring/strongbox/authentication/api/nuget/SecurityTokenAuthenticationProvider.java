package org.carlspring.strongbox.authentication.api.nuget;

import org.carlspring.strongbox.authentication.api.jwt.JwtAuthenticationProvider;
import org.carlspring.strongbox.users.security.JwtClaimsProvider;

/**
 * @author Sergey Bespalov
 *
 */
public class SecurityTokenAuthenticationProvider extends JwtAuthenticationProvider
{
    
    public SecurityTokenAuthenticationProvider(JwtClaimsProvider jwtClaimsProvider)
    {
        super(jwtClaimsProvider);
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return SecurityTokenAuthentication.class.isAssignableFrom(authentication);
    }

}
