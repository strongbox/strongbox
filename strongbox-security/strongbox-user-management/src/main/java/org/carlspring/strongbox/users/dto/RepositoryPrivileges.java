package org.carlspring.strongbox.users.dto;

import java.util.Set;

import org.carlspring.strongbox.users.domain.Privileges;

public interface RepositoryPrivileges
{

    String getRepositoryId();

    Set<Privileges> getRepositoryPrivileges();

    Set<? extends PathPrivileges> getPathPrivileges();

}
