package org.carlspring.strongbox.users.domain;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class User
{

    private final String username;

    private final String password;

    private final boolean enabled;

    private final Set<String> roles;

    private final String securityTokenKey;

    private final AccessModel accessModel;

    public User(final MutableUser other)
    {
        this.username = other.getUsername();
        this.password = other.getPassword();
        this.enabled = other.isEnabled();
        this.roles = immuteRoles(other.getRoles());
        this.securityTokenKey = other.getSecurityTokenKey();
        this.accessModel = immuteAccessModel(other.getAccessModel());
    }

    private Set<String> immuteRoles(final Set<String> source)
    {
        return source != null ? ImmutableSet.copyOf(source) : Collections.emptySet();
    }

    private AccessModel immuteAccessModel(final MutableAccessModel source)
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

    public boolean isEnabled()
    {
        return enabled;
    }

    public Set<String> getRoles()
    {
        return roles;
    }

    public String getSecurityTokenKey()
    {
        return securityTokenKey;
    }

    public AccessModel getAccessModel()
    {
        return accessModel;
    }
}
