package org.carlspring.strongbox.users.dto;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 * @author Przemyslaw Fusik
 */
public class UserAccessModelDto
        implements UserAccessModelReadContract
{

    private Set<UserStorageDto> storages = new LinkedHashSet<>();

    public Set<UserStorageDto> getStorages()
    {
        return storages;
    }

    public Optional<UserStorageDto> getStorage(final String storageId)
    {
        return storages.stream().filter(s -> s.getStorageId().equals(storageId)).findFirst();
    }

}
