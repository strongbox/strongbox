package org.carlspring.strongbox.authentication.api.impl.xml;

import java.util.HashMap;
import java.util.Map;

import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.userdetails.SpringSecurityUser;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Sergey Bespalov
 *
 */
public class SecurityTokenAuthenticator extends JwtAuthenticator
{

    protected Map<String, String> provideTokenClaims(UserDetails userDetails)
    {
        Map<String, String> claimMap = new HashMap<>();
        claimMap.put(User.SECURITY_TOKEN_KEY, ((SpringSecurityUser) userDetails).getSecurityKey());
        
        return claimMap;
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return SecurityTokenAuthentication.class.isAssignableFrom(authentication);
    }

}
