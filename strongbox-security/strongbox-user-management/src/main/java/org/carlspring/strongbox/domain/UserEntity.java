package org.carlspring.strongbox.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.carlspring.strongbox.data.domain.DomainEntity;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.users.dto.User;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author sbespalov
 *
 */
@NodeEntity(Vertices.USER)
public class UserEntity extends DomainEntity implements User
{

    private String password;

    private boolean enabled = true;

    private Set<String> roles = new HashSet<>();

    private String securityTokenKey;

    private LocalDateTime lastUpdated;
    
    private String sourceId;

    UserEntity()
    {
    }

    public UserEntity(String username)
    {
        setUuid(username);
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
    public Set<String> getRoles()
    {
        return roles;
    }

    public void setRoles(Set<String> roles)
    {
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }

    public void addRole(String role)
    {
        roles.add(role);
    }

    public void removeRole(String role)
    {
        roles.remove(role);
    }

    public boolean hasRole(String role)
    {
        return roles.contains(role);
    }

    @Override
    public String getSecurityTokenKey()
    {
        return securityTokenKey;
    }

    public void setSecurityTokenKey(String securityTokenKey)
    {
        this.securityTokenKey = securityTokenKey;
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(final boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public LocalDateTime getLastUpdated()
    {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated)
    {
        this.lastUpdated = lastUpdated;
    }

    public String getSourceId()
    {
        return sourceId;
    }

    public void setSourceId(String source)
    {
        this.sourceId = source;
    }

}
