package org.carlspring.strongbox.users.domain;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;
import org.carlspring.strongbox.users.dto.UserPathPrivilegesDto;
import org.carlspring.strongbox.users.dto.UserRepositoryDto;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import static java.util.stream.Collectors.toSet;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class AccessModelRepository
        implements Serializable
{

    private final String repositoryId;

    private final Set<String> repositoryPrivileges;

    private final Set<AccessModelPathPrivileges> pathPrivileges;

    public AccessModelRepository(final UserRepositoryDto delegate)
    {
        this.repositoryId = delegate.getRepositoryId();
        this.repositoryPrivileges = immuteRepositoryPrivileges(delegate.getRepositoryPrivileges());
        this.pathPrivileges = immutePathPrivileges(delegate.getPathPrivileges());
    }

    private Set<String> immuteRepositoryPrivileges(final Set<PrivilegeDto> source)
    {
        return source != null ?
               ImmutableSet.copyOf(source.stream().map(PrivilegeDto::getName).collect(toSet())) :
               Collections.emptySet();
    }

    private Set<AccessModelPathPrivileges> immutePathPrivileges(final Set<UserPathPrivilegesDto> source)
    {
        return source != null ?
               ImmutableSet.copyOf(source.stream().map(AccessModelPathPrivileges::new).collect(toSet())) :
               Collections.emptySet();
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public Set<String> getRepositoryPrivileges()
    {
        return repositoryPrivileges;
    }

    public Set<AccessModelPathPrivileges> getPathPrivileges()
    {
        return pathPrivileges;
    }
}
