package org.carlspring.strongbox.users.userdetails;

import java.util.Collection;
import java.util.stream.Collectors;

import org.carlspring.strongbox.users.domain.UserData;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class StrongboxUserDetails implements UserDetails
{

    private UserData user;

    public StrongboxUserDetails(UserData user)
    {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return user.getRoles().stream().map(a -> new SimpleGrantedAuthority(a)).collect(Collectors.toSet());
    }

    @Override
    public String getPassword()
    {
        return user.getPassword();
    }

    @Override
    public String getUsername()
    {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @Override
    public boolean isEnabled()
    {
        return user.isEnabled();
    }

    public UserData getUser()
    {
        return user;
    }

}
