package org.carlspring.strongbox.users.service;

import org.carlspring.strongbox.authorization.AuthorizationConfigFileManager;
import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
import org.carlspring.strongbox.authorization.dto.RoleDto;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.UsersConfig;
import org.carlspring.strongbox.users.domain.SystemRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * test for {@link AuthorizationConfigFileManager}
 *
 * @author Bogdan Sukonnov
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = { DataServiceConfig.class,
        UsersConfig.class })
public class AuthorizationConfigFileManagerTest
{

    @Inject
    private AuthorizationConfigFileManager authorizationConfigFileManager;

    private AuthorizationConfigDto config;

    @BeforeEach
    void setup()
            throws IOException
    {
        assertNotNull(authorizationConfigFileManager);
        config = authorizationConfigFileManager.read();
    }

    @Test
    public void testCurrentConfiguration()
            throws IOException
    {
        Map<SystemRole, String> changed = authorizationConfigFileManager.getChangedRestrictedRoles(config);
        assertEquals(0, changed.size());
    }

    @Test
    public void testDeleted()
            throws IOException
    {
        config.setRoles(config.getRoles().stream()
                .filter(r -> !r.getName().equals("ADMIN"))
                .collect(Collectors.toSet()));

        Map<SystemRole, String> changed = authorizationConfigFileManager.getChangedRestrictedRoles(config);
        assertTrue(changed.containsKey(SystemRole.ADMIN));
    }

    @Test
    public void testChanged()
            throws IOException
    {
        RoleDto role = new RoleDto();
        role.setName("ADMIN");
        role.setDescription("changed description");
        Set<RoleDto> roles = new HashSet<>();
        roles.add(role);
        config.setRoles(roles);
        Map<SystemRole, String> changed = authorizationConfigFileManager.getChangedRestrictedRoles(config);
        assertTrue(changed.containsKey(SystemRole.ADMIN));
    }

}
