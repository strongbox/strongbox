package org.carlspring.strongbox.users.domain;

import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserDto;

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

    public User(final UserDto source)
    {
        this.username = source.getUsername();
        this.password = source.getPassword();
        this.enabled = source.isEnabled();
        this.roles = immuteRoles(source.getRoles());
        this.securityTokenKey = source.getSecurityTokenKey();
        this.accessModel = immuteAccessModel(source.getUserAccessModel());
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

    public String getSecurityTokenKey()
    {
        return securityTokenKey;
    }

    public AccessModel getAccessModel()
    {
        return accessModel;
    }

    public boolean isEnabled()
    {
        return enabled;
    }
}
