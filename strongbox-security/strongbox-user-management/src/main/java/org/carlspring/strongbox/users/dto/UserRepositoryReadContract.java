package org.carlspring.strongbox.users.dto;

import java.util.Set;

import org.carlspring.strongbox.authorization.dto.PrivelegieReadContract;

public interface UserRepositoryReadContract
{

    String getRepositoryId();

    Set<? extends PrivelegieReadContract> getRepositoryPrivileges();

    Set<? extends UserPathPrivelegiesReadContract> getPathPrivileges();

}