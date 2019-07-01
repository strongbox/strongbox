package org.carlspring.strongbox.users.domain;

import static java.util.stream.Collectors.toSet;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import org.carlspring.strongbox.users.dto.PathPrivilegesDto;
import org.carlspring.strongbox.users.dto.RepositoryPrivilegesDto;
import org.carlspring.strongbox.users.dto.RepositoryPrivilegesData;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class RepositoryPrivileges
        implements Serializable, RepositoryPrivilegesData
{

    private final String repositoryId;

    private final Set<Privileges> repositoryPrivileges;

    private final Set<PathPrivileges> pathPrivileges;

    public RepositoryPrivileges(final RepositoryPrivilegesDto delegate)
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

    private Set<PathPrivileges> immutePathPrivileges(final Set<PathPrivilegesDto> source)
    {
        return source != null ?
               ImmutableSet.copyOf(source.stream().map(PathPrivileges::new).collect(toSet())) :
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

    public Set<PathPrivileges> getPathPrivileges()
    {
        return pathPrivileges;
    }
}
