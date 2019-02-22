package org.carlspring.strongbox.users.domain;

import static java.util.stream.Collectors.toSet;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang.StringUtils;
import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserAccessModelReadContract;
import org.carlspring.strongbox.users.dto.UserPathPrivelegiesReadContract;
import org.carlspring.strongbox.users.dto.UserRepositoryReadContract;
import org.carlspring.strongbox.users.dto.UserStorageDto;
import org.carlspring.strongbox.users.dto.UserStorageReadContract;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class AccessModel
        implements Serializable, UserAccessModelReadContract
{

    private final Set<Privileges> apiAuthorities;
    
    private final Set<AccessModelStorage> storageAuthorities;


    public AccessModel(UserAccessModelDto delegate)
    {
        this.storageAuthorities = immuteStorages(delegate.getStorageAuthorities());
        this.apiAuthorities = ImmutableSet.copyOf(delegate.getApiAuthorities());
    }

    private Set<AccessModelStorage> immuteStorages(final Set<UserStorageDto> source)
    {
        return source != null ?
               ImmutableSet.copyOf(
                       source.stream().map(AccessModelStorage::new).collect(toSet())) :
               Collections.emptySet();
    }

    @Override
    public Set<Privileges> getApiAuthorities()
    {
        return apiAuthorities;
    }

    public Set<AccessModelStorage> getStorageAuthorities()
    {
        return storageAuthorities;
    }

    @Override
    public Collection<Privileges> getPathPrivileges(String url)
    {
        return getPathPrivileges(url, getStorageAuthorities());
    }
    
    public static Collection<Privileges> getPathPrivileges(String url, Set<? extends UserStorageReadContract> storages)
    {
        String normalizedUrl = StringUtils.chomp(url, "/");

        Collection<Privileges> privileges = new HashSet<>();
        for (final UserStorageReadContract storage : storages)
        {
            String storageKey = "/storages/" + storage.getStorageId();
            if (!normalizedUrl.startsWith(storageKey))
            {
                continue;
            }
            for (UserRepositoryReadContract repository : storage.getRepositories())
            {
                String repositoryKey = storageKey + "/" + repository.getRepositoryId();
                if (!normalizedUrl.startsWith(repositoryKey))
                {
                    continue;
                }
                privileges.addAll(repository.getRepositoryPrivileges());
                for (UserPathPrivelegiesReadContract pathPrivilege : repository.getPathPrivileges())
                {
                    String normalizedPath = StringUtils.chomp(pathPrivilege.getPath(), "/");
                    String pathKey = repositoryKey + "/" + normalizedPath;

                    if (!normalizedUrl.startsWith(pathKey))
                    {
                        continue;
                    }
                    if (normalizedUrl.equals(pathKey) || pathPrivilege.isWildcard())
                    {
                        privileges.addAll(pathPrivilege.getPrivileges());
                    }
                }
            }
        }
        return privileges;
    }
    
}
