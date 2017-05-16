package org.carlspring.strongbox.authentication.api.impl.jwt;

import java.util.Arrays;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

class JWTAuthentication
        extends AbstractAuthenticationToken
{

    private String token;

    public JWTAuthentication(String token)
    {
        super(Arrays.asList(new GrantedAuthority[]{}));
        this.token = token;
    }

    @Override
    public Object getCredentials()
    {
        throw new UnsupportedOperationException("JWTAuthentication is just for token transfer");
    }

    @Override
    public Object getPrincipal()
    {
        throw new UnsupportedOperationException("JWTAuthentication is just for token transfer");
    }

    String getToken()
    {
        return token;
    }

}
