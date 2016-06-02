package org.carlspring.strongbox.rest.app.spring.security;

import org.carlspring.strongbox.data.domain.StrongboxUser;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * A wrapper of {@link StrongboxUser} that is used by Spring Security
 */
class SpringSecurityUser
        implements UserDetails
{

    private String username;

    private String password;

    private boolean enabled;

    private String salt;

    private List<String> roles;

    SpringSecurityUser()
    {
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
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

    public void setRoles(List<String> roles)
    {
        this.roles = roles;
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
               Objects.equal(roles, that.roles);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(username, password, enabled, salt, roles);
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                          .add("username", username)
                          .add("password", password)
                          .add("enabled", enabled)
                          .add("salt", salt)
                          .add("roles", roles)
                          .toString();
    }
}
