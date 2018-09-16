package org.carlspring.strongbox.users.domain;

import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserStorageDto;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import static java.util.stream.Collectors.toSet;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class AccessModel
        implements Serializable
{

    private final Set<AccessModelStorage> storages;


    public AccessModel(final UserAccessModelDto delegate)
    {
        this.storages = immuteStorages(delegate.getStorages());
    }

    private Set<AccessModelStorage> immuteStorages(final Set<UserStorageDto> source)
    {
        return source != null ?
               ImmutableSet.copyOf(
                       source.stream().map(AccessModelStorage::new).collect(toSet())) :
               Collections.emptySet();
    }

    public Set<AccessModelStorage> getStorages()
    {
        return storages;
    }

    public Collection<String> getPathPrivileges(String url)
    {
        String normalizedUrl = StringUtils.chomp(url, "/");

        Collection<String> privileges = new HashSet<>();
        for (final AccessModelStorage storage : storages)
        {
            String storageKey = "/storages/" + storage.getStorageId();
            if (!normalizedUrl.startsWith(storageKey))
            {
                continue;
            }
            for (AccessModelRepository repository : storage.getRepositories())
            {
                String repositoryKey = storageKey + "/" + repository.getRepositoryId();
                if (!normalizedUrl.startsWith(repositoryKey))
                {
                    continue;
                }
                privileges.addAll(repository.getRepositoryPrivileges());
                for (AccessModelPathPrivileges pathPrivilege : repository.getPathPrivileges())
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
