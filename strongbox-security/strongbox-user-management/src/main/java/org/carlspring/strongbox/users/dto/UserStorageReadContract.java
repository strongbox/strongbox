package org.carlspring.strongbox.users.dto;

import java.util.Set;

public interface UserStorageReadContract
{

    Set<? extends UserRepositoryReadContract> getRepositories();

    String getStorageId();

}
