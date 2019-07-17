package org.carlspring.strongbox.users.domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.concurrent.Immutable;

import org.carlspring.strongbox.users.dto.User;
import org.carlspring.strongbox.users.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class UserData implements Serializable, User
{

    public static final String SECURITY_TOKEN_KEY = "security-token-key";
    
    private final String username;

    private final String password;

    private final boolean enabled;

    private final Set<String> roles;

    private final String securityTokenKey;

    private final Date lastUpdate;

    private String sourceId;
    
    public UserData(final UserDetails source) 
    {
        this.username = source.getUsername();
        this.password = source.getPassword();
        this.enabled = source.isEnabled();
        this.roles = source.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toSet());
        this.securityTokenKey = null;
        this.lastUpdate = null;        
    }
    
    public UserData(final UserDto source)
    {
        this.username = source.getUsername();
        this.password = source.getPassword();
        this.enabled = source.isEnabled();
        this.roles = immuteRoles(source.getRoles());
        this.securityTokenKey = source.getSecurityTokenKey();
        this.lastUpdate = immuteDate(source.getLastUpdate());
        this.sourceId = source.getSourceId();
    }

    private Date immuteDate(Date date)
    {
        return date == null ? null : new Date(date.getTime());
    }

    private Set<String> immuteRoles(final Set<String> source)
    {
        return source != null ? ImmutableSet.copyOf(source) : Collections.emptySet();
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    @Override
    public String getPassword()
    {
        return password;
    }

    @Override
    public Set<String> getRoles()
    {
        return roles;
    }

    @Override
    public String getSecurityTokenKey()
    {
        return securityTokenKey;
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public Date getLastUpdate()
    {
        return lastUpdate;
    }

    @Override
    public String getSourceId()
    {
        return sourceId;
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
        sb.append('}');
        return sb.toString();
    }
}
