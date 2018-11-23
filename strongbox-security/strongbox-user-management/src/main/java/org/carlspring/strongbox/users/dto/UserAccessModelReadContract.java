package org.carlspring.strongbox.users.dto;

import java.util.Set;

public interface UserAccessModelReadContract
{

    Set<? extends UserStorageReadContract> getStorages();

}