package org.carlspring.strongbox.authorization.domain;

import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
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

    private final Set<RoleData> roles;

    public AuthorizationConfig(final AuthorizationConfigDto source)
    {
        this.roles = immuteRoles(source.getRoles());
    }

    private Set<RoleData> immuteRoles(final Set<RoleDto> source)
    {
        return source != null ? ImmutableSet.copyOf(source.stream().map(RoleData::new).collect(
                Collectors.toSet())) : Collections.emptySet();
    }

    public Set<RoleData> getRoles()
    {
        return roles;
    }
}

