package org.carlspring.strongbox.users.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.UsersConfig;
import org.carlspring.strongbox.domain.UserEntry;
import org.carlspring.strongbox.users.domain.SystemRole;
import org.carlspring.strongbox.users.userdetails.SpringSecurityUser;
import org.carlspring.strongbox.users.userdetails.UserDetailsMapper;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.Sets;

/**
 * @author ankit.tomar
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = { DataServiceConfig.class,
                                  UsersConfig.class })
public class UserDetailsMapperTest
{

    @Inject
    private UserDetailsMapper userDetailsMapper;

    @Test
    public void testEncodedPasswordUserWithPasswordEncodingAlgoPrefix()
    {
        UserEntry user = new UserEntry();
        user.setUsername("test-user");
        user.setPassword("{bcrypt}$2a$10$WqtVx7Iio0cndyR1lEaKW.SWhUYmF/zHHG5hkAXvH5hUmklM7QfMO");
        user.setRoles(Sets.newHashSet(SystemRole.REPOSITORY_MANAGER.name()));
        user.setEnabled(true);
        SpringSecurityUser securityUser = userDetailsMapper.apply(user);
        assertNotNull(securityUser);
        assertEquals(securityUser.getUsername(), "test-user");
        assertEquals(securityUser.getPassword(),
                     "{bcrypt}$2a$10$WqtVx7Iio0cndyR1lEaKW.SWhUYmF/zHHG5hkAXvH5hUmklM7QfMO");
        assertNotNull(securityUser.getRoles());
    }

    @Test
    public void testEncodedPasswordUserWithoutPasswordEncodingAlgoPrefix()
    {
        UserEntry user = new UserEntry();
        user.setUsername("test-user");
        user.setPassword("$2a$10$WqtVx7Iio0cndyR1lEaKW.SWhUYmF/zHHG5hkAXvH5hUmklM7QfMO");
        user.setRoles(Sets.newHashSet(SystemRole.REPOSITORY_MANAGER.name()));
        user.setEnabled(true);
        SpringSecurityUser securityUser = userDetailsMapper.apply(user);
        assertNotNull(securityUser);
        assertEquals(securityUser.getUsername(), "test-user");
        assertEquals(securityUser.getPassword(),
                     "{bcrypt}$2a$10$WqtVx7Iio0cndyR1lEaKW.SWhUYmF/zHHG5hkAXvH5hUmklM7QfMO");
        assertNotNull(securityUser.getRoles());
    }

}
