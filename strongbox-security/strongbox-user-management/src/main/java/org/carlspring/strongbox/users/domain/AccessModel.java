package org.carlspring.strongbox.users.domain;

import static java.util.stream.Collectors.toSet;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang.StringUtils;
import org.carlspring.strongbox.users.dto.AccessModelDto;
import org.carlspring.strongbox.users.dto.AccessModelData;
import org.carlspring.strongbox.users.dto.PathPrivelegiesData;
import org.carlspring.strongbox.users.dto.RepositoryPrivilegesData;
import org.carlspring.strongbox.users.dto.StoragePrivilegesDto;
import org.carlspring.strongbox.users.dto.StoragePrivilegesData;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class AccessModel
        implements Serializable, AccessModelData
{

    private final Set<Privileges> apiAuthorities;
    
    private final Set<StoragePrivileges> storageAuthorities;


    public AccessModel(AccessModelDto delegate)
    {
        this.storageAuthorities = immuteStorages(delegate.getStorageAuthorities());
        this.apiAuthorities = ImmutableSet.copyOf(delegate.getApiAuthorities());
    }

    private Set<StoragePrivileges> immuteStorages(final Set<StoragePrivilegesDto> source)
    {
        return source != null ?
               ImmutableSet.copyOf(
                       source.stream().map(StoragePrivileges::new).collect(toSet())) :
               Collections.emptySet();
    }

    @Override
    public Set<Privileges> getApiAuthorities()
    {
        return apiAuthorities;
    }

    public Set<StoragePrivileges> getStorageAuthorities()
    {
        return storageAuthorities;
    }

    @Override
    public Collection<Privileges> getPathPrivileges(String url)
    {
        return getPathPrivileges(url, getStorageAuthorities());
    }
    
    public static Collection<Privileges> getPathPrivileges(String url, Set<? extends StoragePrivilegesData> storages)
    {
        String normalizedUrl = StringUtils.chomp(url, "/");

        Collection<Privileges> privileges = new HashSet<>();
        for (final StoragePrivilegesData storage : storages)
        {
            String storageKey = "/storages/" + storage.getStorageId();
            if (!normalizedUrl.startsWith(storageKey))
            {
                continue;
            }
            for (RepositoryPrivilegesData repository : storage.getRepositories())
            {
                String repositoryKey = storageKey + "/" + repository.getRepositoryId();
                if (!normalizedUrl.startsWith(repositoryKey))
                {
                    continue;
                }
                privileges.addAll(repository.getRepositoryPrivileges());
                for (PathPrivelegiesData pathPrivilege : repository.getPathPrivileges())
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
