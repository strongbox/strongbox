package org.carlspring.strongbox.users.security;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.util.Collections;
import java.util.Map;

import javax.inject.Qualifier;

import org.carlspring.strongbox.users.userdetails.SpringSecurityUser;
import org.springframework.stereotype.Component;

@Component
@JwtAuthenticationClaimsProvider.JwtAuthentication
public class JwtAuthenticationClaimsProvider implements JwtClaimsProvider
{

    @Override
    public Map<String, String> getClaims(SpringSecurityUser user)
    {
        return Collections.singletonMap("userHash", String.valueOf(user.hashCode()));
    }

    @Qualifier
    @Retention(RUNTIME)
    public static @interface JwtAuthentication {
        
    }
}
