package org.carlspring.strongbox.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.data.domain.DomainEntity;
import static org.carlspring.strongbox.db.schema.Edges.USER_HAS_USER_ROLE;
import static org.carlspring.strongbox.db.schema.Vertices.USER;
import org.carlspring.strongbox.gremlin.adapters.DateConverter;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import static org.neo4j.ogm.annotation.Relationship.OUTGOING;;
import org.neo4j.ogm.annotation.typeconversion.Convert;

/**
 * @author sbespalov
 *
 */
@NodeEntity(USER)
public class UserEntity extends DomainEntity implements User
{

    private String password;

    private Boolean enabled = true;

    @Relationship(type = USER_HAS_USER_ROLE, direction = OUTGOING)
    private Set<UserRole> roles = new HashSet<>();

    private String securityTokenKey;

    @Convert(DateConverter.class)
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
    public Set<UserRole> getRoles()
    {
        return roles;
    }

    public void setRoles(Set<UserRole> roles)
    {
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }

    public void addRole(String role)
    {
        roles.add(new UserRoleEntity(role));
    }

    public void addRole(UserRole role)
    {
        roles.add(role);
    }

    public void removeRole(UserRole role)
    {
        roles.remove(role);
    }

    public boolean hasRole(UserRole role)
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
    public Boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(final Boolean enabled)
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
