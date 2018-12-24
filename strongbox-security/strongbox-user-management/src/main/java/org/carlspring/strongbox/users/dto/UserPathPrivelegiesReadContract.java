package org.carlspring.strongbox.users.dto;

import java.util.Set;

import org.carlspring.strongbox.authorization.dto.PrivelegieReadContract;

public interface UserPathPrivelegiesReadContract
{

    String getPath();

    boolean isWildcard();

    Set<? extends PrivelegieReadContract> getPrivileges();

}
