package org.carlspring.strongbox.controllers.users;

import org.carlspring.strongbox.users.domain.MutableAccessModel;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
abstract class BaseUserDto
        implements Serializable
{

    protected String username;

    protected boolean enabled;

    protected Set<String> roles;

    protected String securityTokenKey;

    protected MutableAccessModel accessModel;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public Set<String> getRoles()
    {
        return roles == null ? Collections.emptySet() : ImmutableSet.copyOf(roles);
    }

    public void setRoles(Set<String> roles)
    {
        this.roles = roles;
    }

    public String getSecurityTokenKey()
    {
        return securityTokenKey;
    }

    public void setSecurityTokenKey(String securityTokenKey)
    {
        this.securityTokenKey = securityTokenKey;
    }

    public MutableAccessModel getAccessModel()
    {
        return accessModel;
    }

    public void setAccessModel(MutableAccessModel accessModel)
    {
        this.accessModel = accessModel;
    }
}
