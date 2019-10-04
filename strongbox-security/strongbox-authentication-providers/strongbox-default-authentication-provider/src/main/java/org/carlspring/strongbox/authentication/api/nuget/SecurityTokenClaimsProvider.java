package org.carlspring.strongbox.authentication.api.nuget;

import java.util.Collections;
import java.util.Map;

import org.carlspring.strongbox.users.domain.UserData;
import org.carlspring.strongbox.users.security.JwtClaimsProvider;
import org.carlspring.strongbox.users.userdetails.SpringSecurityUser;

public class SecurityTokenClaimsProvider implements JwtClaimsProvider
{

    @Override
    public Map<String, String> getClaims(SpringSecurityUser user)
    {
        return Collections.singletonMap(UserData.SECURITY_TOKEN_KEY, user.getSecurityKey());
    }

}
