package org.carlspring.strongbox.users.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Predefined set of privileges which are not modifiable but assignable
 * to any {@link org.carlspring.strongbox.users.domain.User} entity.
 *
 * @author Alex Oreshkevich
 * @see {@linkplain https://dev.carlspring.org/youtrack/issue/SB-122}
 * @see {@linkplain https://dev.carlspring.org/youtrack/issue/SB-126}
 */
public enum Roles
{
    ADMIN(Privileges.all()),

    REPOSITORY_MANAGER(Privileges.repoAll()),

    ARTIFACTS_MANAGER(Privileges.artifactsAll()),

    USER_MANAGER(Privileges.usersAll()),

    TOKEN_MANAGER(Privileges.tokenAll()),

    LOGS_MANAGER(Privileges.logsAll()),

    UI_MANAGER(Privileges.uiAll());

    private Set<Privileges> privileges;

    /**
     * Define custom privilege based on one or multiple privileges.
     *
     * @param privileges
     */
    private Roles(Privileges... privileges)
    {
        this(Arrays.asList(privileges));
    }

    private Roles(Collection<? extends Privileges> privileges)
    {
        this.privileges = new HashSet<>(privileges);
    }

    public Set<Privileges> getPrivileges()
    {
        return privileges;
    }
}
