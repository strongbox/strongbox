package org.carlspring.strongbox.users.dto;

import java.util.Set;

import org.carlspring.strongbox.users.domain.Privileges;

public interface UserRepositoryReadContract
{

    String getRepositoryId();

    Set<Privileges> getRepositoryPrivileges();

    Set<? extends UserPathPrivelegiesReadContract> getPathPrivileges();

}
