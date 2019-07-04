package org.carlspring.strongbox.users.dto;


import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.carlspring.strongbox.users.domain.AccessModelData;
import org.carlspring.strongbox.users.domain.Privileges;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 * @author Przemyslaw Fusik
 */
public class AccessModelDto
        implements AccessModel
{

    private Set<Privileges> apiAuthorities = new LinkedHashSet<>();    
    
    private Set<StoragePrivilegesDto> storageAuthorities = new LinkedHashSet<>();


    public Set<Privileges> getApiAuthorities()
    {
        return apiAuthorities;
    }

    public Set<StoragePrivilegesDto> getStorageAuthorities()
    {
        return storageAuthorities;
    }

    public Optional<StoragePrivilegesDto> getStorageAuthorities(final String storageId)
    {
        return storageAuthorities.stream().filter(s -> s.getStorageId().equals(storageId)).findFirst();
    }

    @Override
    public Set<Privileges> getPathAuthorities(String url)
    {
        return AccessModelData.getPathAuthorities(url, storageAuthorities);
    }
    
}
