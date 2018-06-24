package org.carlspring.strongbox.authorization.domain;

import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
import org.carlspring.strongbox.authorization.dto.PrivilegeDto;
import org.carlspring.strongbox.authorization.dto.RoleDto;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class AuthorizationConfig
{

    private final Set<Role> roles;

    private final Set<Privilege> privileges;

    public AuthorizationConfig(final AuthorizationConfigDto source)
    {
        this.roles = immuteRoles(source.getRoles());
        this.privileges = immutePrivileges(source.getPrivileges());
    }

    private Set<Role> immuteRoles(final Set<RoleDto> source)
    {
        return source != null ? ImmutableSet.copyOf(source.stream().map(Role::new).collect(
                Collectors.toSet())) : Collections.emptySet();
    }

    private Set<Privilege> immutePrivileges(final Set<PrivilegeDto> source)
    {
        return source != null ? ImmutableSet.copyOf(source.stream().map(Privilege::new).collect(
                Collectors.toSet())) : Collections.emptySet();
    }

    public Set<Role> getRoles()
    {
        return roles;
    }

    public Set<Privilege> getPrivileges()
    {
        return privileges;
    }
}
