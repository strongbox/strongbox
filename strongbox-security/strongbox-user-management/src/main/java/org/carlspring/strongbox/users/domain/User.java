package org.carlspring.strongbox.users.domain;

import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserAccessModelReadContract;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.dto.UserReadContract;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.concurrent.Immutable;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class User implements Serializable, UserReadContract
{

    public static final String SECURITY_TOKEN_KEY = "security-token-key";
    
    private final String username;

    private final String password;

    private final boolean enabled;

    private final Set<String> roles;

    private final Set<String> authorities;

    private final String securityTokenKey;

    private final UserAccessModelReadContract accessModel;

    private final Date lastUpdate;
    
    public User(final UserDetails source) {
        this.username = source.getUsername();
        this.password = source.getPassword();
        this.enabled = source.isEnabled();
        this.roles = source.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toSet());
        this.authorities = null;
        this.securityTokenKey = null;
        this.accessModel = null;
        this.lastUpdate = null;        
    }
    
    public User(final UserDto source)
    {
        this.username = source.getUsername();
        this.password = source.getPassword();
        this.enabled = source.isEnabled();
        this.roles = immuteRoles(source.getRoles());
        this.authorities = source.getAuthorities();
        this.securityTokenKey = source.getSecurityTokenKey();
        this.accessModel = source.getUserAccessModel();
        this.lastUpdate = immuteDate(source.getLastUpdate());
    }

    private Date immuteDate(Date date)
    {
        return date == null ? null : new Date(date.getTime());
    }

    private Set<String> immuteRoles(final Set<String> source)
    {
        return source != null ? ImmutableSet.copyOf(source) : Collections.emptySet();
    }

    private AccessModel immuteAccessModel(final UserAccessModelDto source)
    {
        return source != null ? new AccessModel(source) : null;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public Set<String> getRoles()
    {
        return roles;
    }

    public Set<String> getAuthorities()
    {
        return authorities;
    }

    public String getSecurityTokenKey()
    {
        return securityTokenKey;
    }

    public UserAccessModelReadContract getUserAccessModel()
    {
        return accessModel;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public Date getLastUpdate()
    {
        return lastUpdate;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("username='")
          .append(username)
          .append('\'');
        sb.append(", roles=")
          .append(roles);
        sb.append(", authorities=")
          .append(authorities);
        sb.append(", userAccessModel=")
          .append(accessModel);
        sb.append('}');
        return sb.toString();
    }
}
