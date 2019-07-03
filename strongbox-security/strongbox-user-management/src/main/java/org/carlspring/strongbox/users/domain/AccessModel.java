package org.carlspring.strongbox.users.domain;

import static java.util.stream.Collectors.toSet;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang.StringUtils;
import org.carlspring.strongbox.users.dto.AccessModelData;
import org.carlspring.strongbox.users.dto.AccessModelDto;
import org.carlspring.strongbox.users.dto.PathPrivilegesData;
import org.carlspring.strongbox.users.dto.RepositoryPrivilegesData;
import org.carlspring.strongbox.users.dto.StoragePrivilegesData;
import org.carlspring.strongbox.users.dto.StoragePrivilegesDto;

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
    public Set<Privileges> getPathAuthorities(String url)
    {
        return getPathAuthorities(url, getStorageAuthorities());
    }
    
    public static Set<Privileges> getPathAuthorities(String url, Set<? extends StoragePrivilegesData> storages)
    {
        String normalizedUrl = StringUtils.chomp(url, "/");

        Set<Privileges> privileges = new HashSet<>();
        for (final StoragePrivilegesData storage : storages)
        {
            String storageKey = "/storages/" + storage.getStorageId();
            if (!normalizedUrl.startsWith(storageKey))
            {
                continue;
            }
            for (RepositoryPrivilegesData repository : storage.getRepositoryPrivileges())
            {
                String repositoryKey = storageKey + "/" + repository.getRepositoryId();
                if (!normalizedUrl.startsWith(repositoryKey))
                {
                    continue;
                }
                privileges.addAll(repository.getRepositoryPrivileges());
                for (PathPrivilegesData pathPrivilege : repository.getPathPrivileges())
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
