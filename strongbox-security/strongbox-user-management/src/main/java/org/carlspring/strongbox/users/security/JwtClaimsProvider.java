package org.carlspring.strongbox.users.security;

import java.util.Map;

import org.carlspring.strongbox.users.userdetails.SpringSecurityUser;

public interface JwtClaimsProvider
{

    Map<String, String> getClaims(SpringSecurityUser user);
    
}
