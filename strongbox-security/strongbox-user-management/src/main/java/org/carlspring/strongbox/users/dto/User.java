package org.carlspring.strongbox.users.dto;

import java.time.LocalDateTime;
import java.util.Set;

import org.carlspring.strongbox.data.domain.DomainObject;

public interface User extends DomainObject
{

    default String getUsername() {
        return getUuid();
    }

    String getPassword();

    Set<String> getRoles();

    String getSecurityTokenKey();

    boolean isEnabled();

    LocalDateTime getLastUpdated();

    String getSourceId();

}