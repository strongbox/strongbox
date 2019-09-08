package org.carlspring.strongbox.users.domain;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Defines the restricted system roles. The purpose is to restrict changing or
 * deleting some of the configured roles.
 * Restrictions works only for the roles with set hash.
 * 
 * @author sbespalov
 */
public enum SystemRole
{
    ADMIN("896305b428e36f2ca3a5dbbcb8a27bd0"),
    UI_MANAGER,
    REPOSITORY_MANAGER,
    ARTIFACTS_MANAGER,
    ANONYMOUS;

    /**
     * MD5 hash of the RoleDTO object with the same name
     *
     */
    private String hash;

    private SystemRole() {}

    private SystemRole(String hash)
    {
        this.hash = hash;
    }

    public static Map<String, SystemRole> getRestricted()
    {
        return Stream.of(SystemRole.values())
                .filter(v -> v.getHash() != null)
                .collect(Collectors.toMap(SystemRole::toString, v -> v));
    }

    public String getHash()
    {
        return hash;
    }
}
