package org.carlspring.strongbox.users.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author mtodorov
 */
public class UserDto
        implements Serializable, UserReadContract
{

    private String username;

    private String password;

    private boolean enabled = true;

    private Set<String> roles = new HashSet<>();

    private Set<String> authorities = new HashSet<>();

    @JsonProperty(value = "accessModel")
    @JsonDeserialize(as = UserAccessModelDto.class)
    private UserAccessModelReadContract userAccessModel;

    private String securityTokenKey;

    @JsonIgnore
    private Date lastUpdate;

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
    public Set<String> getAuthorities()
    {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities)
    {
        this.authorities = authorities;
    }

    @Override
    public UserAccessModelReadContract getUserAccessModel()
    {
        return userAccessModel;
    }

    public void setUserAccessModel(UserAccessModelReadContract userAccessModel)
    {
        this.userAccessModel = userAccessModel;
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
    public Date getLastUpdate()
    {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate)
    {
        this.lastUpdate = lastUpdate;
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
        sb.append(", userAccessModel=")
          .append(userAccessModel);
        sb.append('}');
        return sb.toString();
    }
}
