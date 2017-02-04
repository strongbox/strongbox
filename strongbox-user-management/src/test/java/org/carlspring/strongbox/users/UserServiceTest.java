package org.carlspring.strongbox.users;

import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.UsersConfig;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

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

        User user = new User();
        user.setEnabled(true);
        String testUserName = "TEST";
        user.setUsername(testUserName);
        user.setPassword("test-pwd");
        assertNull(user.getObjectId());

        final User storedUser = userService.save(user);
        assertNotNull(storedUser);
        String id = storedUser.getObjectId();
        assertNotNull(id);

        logger.debug("Saved user: " + storedUser);

        User foundEntity = userService.findByUserName(testUserName);
        assertNotNull("Unable to locate user " + testUserName + ". Save operation fails...", foundEntity);

        logger.debug("Found stored user\n\t" + foundEntity + "\n");
        assertEquals(storedUser.getObjectId(), foundEntity.getObjectId());
        assertEquals(storedUser.getUsername(), foundEntity.getUsername());
        assertEquals(storedUser.getPassword(), foundEntity.getPassword());
        assertEquals(storedUser.isEnabled(), foundEntity.isEnabled());
    }

    @Test
    public void testThatUserNameIsUnique()
    {

        // find any existing user and try to save it again
        User first = userService.findAll()
                                .orElseThrow(() -> new NullPointerException("No users are available"))
                                .get(0);
        first.setEnabled(!first.isEnabled());

        logger.debug("Trying to save " + first);
        userService.save(first);
    }

    @Test
    public void displayUsers()
    {
        userService.findAll()
                   .ifPresent(strongboxUsers -> strongboxUsers.forEach(user -> logger.debug(user.toString())));
    }

}
