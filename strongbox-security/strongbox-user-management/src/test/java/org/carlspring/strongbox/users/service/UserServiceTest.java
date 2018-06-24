package org.carlspring.strongbox.users.service;

import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.UsersConfig;
import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.dto.UserDto;

import javax.inject.Inject;
import java.util.Collection;
import java.util.stream.Collectors;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DataServiceConfig.class,
                                  UsersConfig.class })
public class UserServiceTest
{

    private static final Logger logger = LoggerFactory.getLogger(UserServiceTest.class);

    @Inject
    UserService userService;

    @Before
    public void setup()
    {
        assertNotNull(userService);
    }

    @Test
    public void testCreateAndDeleteUserOperations()
            throws Exception
    {
        String testUserName = "test-user";

        UserDto user = new UserDto();
        user.setEnabled(true);
        user.setUsername(testUserName);
        user.setPassword("test-password");

        userService.add(user);

        User foundEntity = userService.findByUserName(testUserName);

        assertNotNull("Unable to locate user " + testUserName + ". Save operation failed!", foundEntity);

        logger.debug("Found stored user\n\t" + foundEntity + "\n");

        assertEquals(testUserName, foundEntity.getUsername());
        assertEquals("test-password", foundEntity.getPassword());
        assertEquals(true, foundEntity.isEnabled());
    }

    @Test
    public void testThatUserNameIsUnique()
    {
        assertThat(userService.findAll().getUsers().stream().filter(u -> "admin".equals(u.getUsername())).collect(
                Collectors.toList()).size(), CoreMatchers.equalTo(1));

        UserDto user = new UserDto();
        user.setUsername("admin");

        userService.add(user);

        assertThat(userService.findAll().getUsers().stream().filter(u -> "admin".equals(u.getUsername())).collect(
                Collectors.toList()).size(), CoreMatchers.equalTo(1));
    }

    @Test
    public void testPrivilegesProcessingForAccessModel()
    {
        // Load the user
        User user = userService.findByUserName("developer01");

        assertNotNull("Unable to find user by name developer01", user);

        // Display the access model
        AccessModel accessModel = user.getAccessModel();

        logger.debug(accessModel.toString());

        assertNotNull(accessModel.getWildCardPrivilegesMap());
        assertFalse(accessModel.getWildCardPrivilegesMap().isEmpty());

        // Make sure that the privileges were correctly assigned for the example paths
        Collection<String> privileges;

        privileges = accessModel.getPathPrivileges("/storages/storage0/releases/" +
                                                   "org/carlspring/foo/1.1/foo-1.1.jar");

        assertNotNull(privileges);
        assertFalse(privileges.isEmpty());
        assertTrue(privileges.contains("ARTIFACTS_RESOLVE"));

        privileges = accessModel.getPathPrivileges("/storages/storage0/releases/" +
                                                   "com/carlspring/foo/1.2/foo-1.2.jar");

        assertNotNull(privileges);
        assertFalse(privileges.isEmpty());
        assertTrue(privileges.contains("ARTIFACTS_RESOLVE"));
        assertTrue(privileges.contains("ARTIFACTS_VIEW"));
    }

}
