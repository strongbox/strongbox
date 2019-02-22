package org.carlspring.strongbox.users.domain;

import static java.util.stream.Collectors.toSet;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import org.carlspring.strongbox.users.dto.UserPathPrivilegesDto;
import org.carlspring.strongbox.users.dto.UserRepositoryDto;
import org.carlspring.strongbox.users.dto.UserRepositoryReadContract;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class AccessModelRepository
        implements Serializable, UserRepositoryReadContract
{

    private final String repositoryId;

    private final Set<Privileges> repositoryPrivileges;

    private final Set<AccessModelPathPrivileges> pathPrivileges;

    public AccessModelRepository(final UserRepositoryDto delegate)
    {
        this.repositoryId = delegate.getRepositoryId();
        this.repositoryPrivileges = immuteRepositoryPrivileges(delegate.getRepositoryPrivileges());
        this.pathPrivileges = immutePathPrivileges(delegate.getPathPrivileges());
    }

    private Set<Privileges> immuteRepositoryPrivileges(final Set<Privileges> set)
    {
        return set != null ? Collections.unmodifiableSet(set)
                : Collections.emptySet();
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

    public Set<Privileges> getRepositoryPrivileges()
    {
        return repositoryPrivileges;
    }

    public Set<AccessModelPathPrivileges> getPathPrivileges()
    {
        return pathPrivileges;
    }
}
