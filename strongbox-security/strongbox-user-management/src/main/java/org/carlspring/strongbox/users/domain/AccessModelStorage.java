package org.carlspring.strongbox.users.domain;

import org.carlspring.strongbox.users.dto.UserRepositoryDto;
import org.carlspring.strongbox.users.dto.UserStorageDto;

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
public class AccessModelStorage
        implements Serializable
{

    private final Set<AccessModelRepository> repositories;

    private final String storageId;

    public AccessModelStorage(final UserStorageDto delegate)
    {
        this.repositories = immuteRepositories(delegate.getRepositories());
        this.storageId = delegate.getStorageId();
    }

    private Set<AccessModelRepository> immuteRepositories(final Set<UserRepositoryDto> source)
    {
        return source != null ?
               ImmutableSet.copyOf(source.stream().map(AccessModelRepository::new).collect(toSet())) :
               Collections.emptySet();
    }

    public Set<AccessModelRepository> getRepositories()
    {
        return repositories;
    }

    public String getStorageId()
    {
        return storageId;
    }
}
