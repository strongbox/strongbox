package org.carlspring.strongbox.users.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Predefined set of privileges which are not modifiable but assignable
 * to any {@link org.carlspring.strongbox.users.data.domain.User} entity.
 *
 * @author Alex Oreshkevich
 * @see {@linkplain https://dev.carlspring.org/youtrack/issue/SB-122} for details
 */
public enum Privileges
{

    ADMIN(Roles.adminAll()),

    REPOSITORY_MANAGER(Roles.repoAll()),

    ARTIFACTS_MANAGER(Roles.artifactsAll()),

    USER_MANAGER(Roles.usersAll()),

    TOKEN_MANAGER(Roles.tokenAll()),

    LOGS_MANAGER(Roles.logsAll()),

    UI_MANAGER(Roles.uiAll());

    private Set<Roles> roles;

    /**
     * Define custom privilege based on one or multiple roles.
     *
     * @param roles
     */
    private Privileges(Roles... roles)
    {
        this(Arrays.asList(roles));
    }

    private Privileges(Collection<? extends Roles> roles)
    {
        this.roles = new HashSet<>(roles);
    }
}
