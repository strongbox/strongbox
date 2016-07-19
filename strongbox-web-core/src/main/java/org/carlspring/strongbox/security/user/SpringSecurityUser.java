package org.carlspring.strongbox.security.user;

import org.carlspring.strongbox.users.domain.User;

import java.util.Collection;

import com.google.common.base.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * A wrapper of {@link User} that is used by Spring Security
 */
class SpringSecurityUser
        implements UserDetails
{

    private String username;

    private String password;

    private boolean enabled;

    private String salt;

    private Collection<? extends GrantedAuthority> authorities;

    SpringSecurityUser()
    {
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return enabled;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return enabled;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    @Override
    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getSalt()
    {
        return salt;
    }

    public void setSalt(String salt)
    {
        this.salt = salt;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return authorities;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities)
    {
        this.authorities = authorities;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpringSecurityUser that = (SpringSecurityUser) o;
        return enabled == that.enabled &&
               Objects.equal(username, that.username) &&
               Objects.equal(password, that.password) &&
               Objects.equal(salt, that.salt) &&
               Objects.equal(authorities, that.authorities);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(username, password, enabled, salt, authorities);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("SpringSecurityUser{");
        sb.append("\n\tusername='").append(username).append('\'');
        sb.append(", \n\tpassword='").append(password).append('\'');
        sb.append(", \n\tenabled=").append(enabled);
        sb.append(", \n\tsalt='").append(salt).append('\'');
        sb.append(", \n\tauthorities=").append(authorities);
        sb.append('}');
        return sb.toString();
    }
}
