package org.carlspring.strongbox.users.domain;

import org.carlspring.strongbox.authorization.dto.PrivelegieReadContract;
import org.carlspring.strongbox.authorization.dto.PrivilegeDto;
import org.carlspring.strongbox.users.dto.UserPathPrivilegesDto;
import org.carlspring.strongbox.users.dto.UserRepositoryDto;
import org.carlspring.strongbox.users.dto.UserRepositoryReadContract;

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
        implements Serializable, UserRepositoryReadContract
{

    private final String repositoryId;

    private final Set<AccessModelPrivilege> repositoryPrivileges;

    private final Set<AccessModelPathPrivileges> pathPrivileges;

    public AccessModelRepository(final UserRepositoryDto delegate)
    {
        this.repositoryId = delegate.getRepositoryId();
        this.repositoryPrivileges = immuteRepositoryPrivileges(delegate.getRepositoryPrivileges());
        this.pathPrivileges = immutePathPrivileges(delegate.getPathPrivileges());
    }

    private Set<AccessModelPrivilege> immuteRepositoryPrivileges(final Set<PrivilegeDto> set)
    {
        return set != null ? set.stream().map(p -> new AccessModelPrivilege(p)).collect(toSet())
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

    public Set<? extends PrivelegieReadContract> getRepositoryPrivileges()
    {
        return repositoryPrivileges;
    }

    public Set<AccessModelPathPrivileges> getPathPrivileges()
    {
        return pathPrivileges;
    }
}
