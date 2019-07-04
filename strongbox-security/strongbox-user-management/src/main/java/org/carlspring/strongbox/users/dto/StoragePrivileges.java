package org.carlspring.strongbox.users.dto;

import java.util.Set;

public interface StoragePrivileges
{

    Set<? extends RepositoryPrivileges> getRepositoryPrivileges();

    String getStorageId();

}
