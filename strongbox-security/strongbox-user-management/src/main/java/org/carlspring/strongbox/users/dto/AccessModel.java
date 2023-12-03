package org.carlspring.strongbox.users.dto;

import java.io.Serializable;
import java.util.Set;

import org.carlspring.strongbox.users.domain.Privileges;

public interface AccessModel extends Serializable
{
    Set<Privileges> getApiAuthorities();

    Set<Privileges> getPathAuthorities(String path);

}
