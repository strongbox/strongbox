package org.carlspring.strongbox.users.domain;

import org.carlspring.strongbox.users.dto.RepositoryPrivilegesDto;
import org.carlspring.strongbox.users.dto.StoragePrivilegesDto;
import org.carlspring.strongbox.users.dto.StoragePrivilegesData;

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
public class StoragePrivileges
        implements Serializable, StoragePrivilegesData
{

    private final Set<RepositoryPrivileges> repositories;

    private final String storageId;

    public StoragePrivileges(final StoragePrivilegesDto delegate)
    {
        this.repositories = immuteRepositories(delegate.getRepositories());
        this.storageId = delegate.getStorageId();
    }

    private Set<RepositoryPrivileges> immuteRepositories(final Set<RepositoryPrivilegesDto> source)
    {
        return source != null ?
               ImmutableSet.copyOf(source.stream().map(RepositoryPrivileges::new).collect(toSet())) :
               Collections.emptySet();
    }

    public Set<RepositoryPrivileges> getRepositories()
    {
        return repositories;
    }

    public String getStorageId()
    {
        return storageId;
    }
}
