package org.carlspring.strongbox.users.service;

import org.carlspring.strongbox.authorization.AuthorizationConfigFileManager;
import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
import org.carlspring.strongbox.authorization.dto.RoleDto;
import org.carlspring.strongbox.users.domain.SystemRole;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * test for {@link AuthorizationConfigFileManager}
 *
 * @author Bogdan Sukonnov
 */
public class AuthorizationConfigFileManagerTest
{
    private AuthorizationConfigFileManager authorizationConfigFileManager =
            new AuthorizationConfigFileManager(contextClasses -> null);

    @Test
    public void testCurrentConfiguration()
            throws IOException
    {
        AuthorizationConfigDto config = getConfig("ADMIN", "Super User role with all possible privileges");
        Set<SystemRole> changed = authorizationConfigFileManager.getChangedRestrictedRoles(config);
        assertEquals(0, changed.size());
    }

    @Test
    public void testDeleted()
            throws IOException
    {
        AuthorizationConfigDto config = getConfig("NotRestrictedRole", "not restricted");
        Set<SystemRole> changed = authorizationConfigFileManager.getChangedRestrictedRoles(config);
        assertTrue(changed.contains(SystemRole.ADMIN));
    }

    @Test
    public void testChanged()
            throws IOException
    {
        AuthorizationConfigDto config = getConfig("ADMIN", "changed description");
        Set<SystemRole> changed = authorizationConfigFileManager.getChangedRestrictedRoles(config);
        assertTrue(changed.contains(SystemRole.ADMIN));
    }

    private AuthorizationConfigDto getConfig(String roleName, String roleDescription)
    {
        // set up role
        RoleDto role = new RoleDto();
        role.setName(roleName);
        role.setDescription(roleDescription);

        // add role to Set
        Set<RoleDto> roles = new HashSet<>();
        roles.add(role);

        // set up config
        AuthorizationConfigDto config = new AuthorizationConfigDto();
        config.setRoles(roles);

        return config;
    }

}
