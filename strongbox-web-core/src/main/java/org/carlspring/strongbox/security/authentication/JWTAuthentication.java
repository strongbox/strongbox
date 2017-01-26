package org.carlspring.strongbox.security.authentication;

import java.util.Arrays;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class JWTAuthentication
        extends AbstractAuthenticationToken
{

    private String token;

    private UserDetails userDetails;

    public JWTAuthentication(String token)
    {
        super(Arrays.asList(new GrantedAuthority[]{}));
        this.token = token;
    }

    @Override
    public Object getCredentials()
    {
        return "";
    }

    @Override
    public Object getPrincipal()
    {
        return userDetails;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

}
