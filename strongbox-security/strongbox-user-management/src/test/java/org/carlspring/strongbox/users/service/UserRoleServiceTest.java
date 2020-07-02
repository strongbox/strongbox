package org.carlspring.strongbox.users.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.UsersConfig;
import org.carlspring.strongbox.domain.UserRole;
import org.carlspring.strongbox.domain.UserRoleEntity;
import org.carlspring.strongbox.repositories.UserRoleRepository;
import org.carlspring.strongbox.users.domain.SystemRole;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author ankit.tomar
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = { DataServiceConfig.class,
                                  UsersConfig.class })
public class UserRoleServiceTest
{

    @Inject
    private UserRoleRepository userRoleRepository;

    @Inject
    private UserRoleService userRoleService;

    @Test
    public void testFetchExistingRole()
    {
        userRoleRepository.save(new UserRoleEntity(SystemRole.ADMIN.name()));

        UserRole userRolefromDB = userRoleService.findOneOrCreate(SystemRole.ADMIN.name());
        assertNotNull(userRolefromDB);
        assertEquals(userRolefromDB.getRoleName(), SystemRole.ADMIN.name());
    }

    @Test
    public void testFetchNonExistingRole()
    {
        UserRole userRolefromDB = userRoleService.findOneOrCreate(SystemRole.REPOSITORY_MANAGER.name());
        assertNotNull(userRolefromDB);
        assertEquals(userRolefromDB.getRoleName(), SystemRole.REPOSITORY_MANAGER.name());
    }
}
