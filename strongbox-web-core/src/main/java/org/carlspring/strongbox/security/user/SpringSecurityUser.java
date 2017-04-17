package org.carlspring.strongbox.security.user;

import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.domain.User;

import java.util.Collection;

import com.google.common.base.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * A wrapper of {@link User} that is used by Spring Security
 */
public class SpringSecurityUser
        implements UserDetails
{

    private String username;

    private String password;

    private boolean enabled;

    private String salt;

    private Collection<? extends GrantedAuthority> authorities;

    private AccessModel accessModel;

    private String url;

    SpringSecurityUser()
    {
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
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

    public AccessModel getAccessModel()
    {
        return accessModel;
    }

    public void setAccessModel(AccessModel accessModel)
    {
        this.accessModel = accessModel;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpringSecurityUser user = (SpringSecurityUser) o;
        return enabled == user.enabled &&
               Objects.equal(username, user.username) &&
               Objects.equal(password, user.password) &&
               Objects.equal(salt, user.salt) &&
               Objects.equal(authorities, user.authorities) &&
               Objects.equal(accessModel, user.accessModel) &&
               Objects.equal(url, user.url);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(username, password, enabled, salt, authorities, accessModel, url);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("SpringSecurityUser{");
        sb.append("username='")
          .append(username)
          .append('\'');
        sb.append(", password='")
          .append(password)
          .append('\'');
        sb.append(", enabled=")
          .append(enabled);
        sb.append(", salt='")
          .append(salt)
          .append('\'');
        sb.append(", authorities=")
          .append(authorities);
        sb.append(", accessModel=")
          .append(accessModel);
        sb.append(", url='")
          .append(url)
          .append('\'');
        sb.append('}');
        return sb.toString();
    }
}
