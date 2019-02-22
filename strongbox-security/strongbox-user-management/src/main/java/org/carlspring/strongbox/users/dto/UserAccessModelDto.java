package org.carlspring.strongbox.users.dto;


import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.domain.Privileges;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 * @author Przemyslaw Fusik
 */
public class UserAccessModelDto
        implements UserAccessModelReadContract
{

    private Set<Privileges> apiAuthorities = new LinkedHashSet<>();    
    
    private Set<UserStorageDto> storageAuthorities = new LinkedHashSet<>();


    public Set<Privileges> getApiAuthorities()
    {
        return apiAuthorities;
    }

    public Set<UserStorageDto> getStorageAuthorities()
    {
        return storageAuthorities;
    }

    public Optional<UserStorageDto> getStorage(final String storageId)
    {
        return storageAuthorities.stream().filter(s -> s.getStorageId().equals(storageId)).findFirst();
    }

    @Override
    public Collection<Privileges> getPathPrivileges(String url)
    {
        return AccessModel.getPathPrivileges(url, storageAuthorities);
    }
    
}
