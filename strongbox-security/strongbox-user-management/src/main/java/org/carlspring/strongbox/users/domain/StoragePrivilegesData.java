package org.carlspring.strongbox.users.domain;

import static java.util.stream.Collectors.toSet;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import org.carlspring.strongbox.users.dto.RepositoryPrivilegesDto;
import org.carlspring.strongbox.users.dto.StoragePrivileges;
import org.carlspring.strongbox.users.dto.StoragePrivilegesDto;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class StoragePrivilegesData
        implements Serializable, StoragePrivileges
{

    private final Set<RepositoryPrivilegesData> repositories;

    private final String storageId;

    public StoragePrivilegesData(final StoragePrivilegesDto delegate)
    {
        this.repositories = immuteRepositories(delegate.getRepositoryPrivileges());
        this.storageId = delegate.getStorageId();
    }

    private Set<RepositoryPrivilegesData> immuteRepositories(final Set<RepositoryPrivilegesDto> source)
    {
        return source != null ?
               ImmutableSet.copyOf(source.stream().map(RepositoryPrivilegesData::new).collect(toSet())) :
               Collections.emptySet();
    }

    public Set<RepositoryPrivilegesData> getRepositoryPrivileges()
    {
        return repositories;
    }

    public String getStorageId()
    {
        return storageId;
    }
}
