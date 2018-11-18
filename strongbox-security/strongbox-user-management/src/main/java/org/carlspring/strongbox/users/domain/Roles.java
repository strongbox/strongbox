package org.carlspring.strongbox.users.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Predefined set of privileges which are not modifiable but assignable
 * to any User entity.
 *
 * @author Alex Oreshkevich
 * @see {@linkplain https://dev.carlspring.org/youtrack/issue/SB-122}
 * @see {@linkplain https://dev.carlspring.org/youtrack/issue/SB-126}
 */
public enum Roles
{
    ADMIN(Privileges.all(), "Role with all privileges"),

    // This is temporary and will be refactored as part of SB-1220 in a later stage.
    GLOBAL_CONFIGURATION_MANAGER(Privileges.configurationAll(), "Role with all global configuration related privileges"),

    REPOSITORY_MANAGER(Privileges.repoAll(), "Role with all repository related privileges"),

    ARTIFACTS_MANAGER(Privileges.artifactsAll(), "Role with all artifact related privileges"),

    USER_MANAGER(Privileges.usersAll(), "Role with all user related privileges"),

    TOKEN_MANAGER(Privileges.tokenAll(), "Role with all token related privileges"),

    LOGS_MANAGER(Privileges.logsAll(), "Role with all logs related privileges"),

    UI_MANAGER(Privileges.uiAll(), "Role with all user interface related privileges");

    private Set<Privileges> privileges;

    private String description;

    Roles(Collection<? extends Privileges> privileges,
          String description)
    {
        this.privileges = new HashSet<>(privileges);
        this.description = description;
    }

    public Set<Privileges> getPrivileges()
    {
        return privileges;
    }

    public String getDescription()
    {
        return description;
    }
}
