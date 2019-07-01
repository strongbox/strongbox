package org.carlspring.strongbox.users.dto;

import java.util.Set;

public interface StoragePrivilegesData
{

    Set<? extends RepositoryPrivilegesData> getRepositories();

    String getStorageId();

}
