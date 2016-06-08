package org.carlspring.strongbox.users;

import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@UserServiceTestContext
@RunWith(SpringJUnit4ClassRunner.class)
public class OrientCrudRepositoryTest
{

    private static final Logger logger = LoggerFactory.getLogger(OrientCrudRepositoryTest.class);

    final String testUserName = "TEST";

    @Autowired
    UserService userService;

    @Before
    public void setup()
    {
        assertNotNull(userService);
    }

    @Test
    @Transactional
    public synchronized void testCreateAndDeleteUserOperations()
            throws Exception
    {

        final User user = new User();
        user.setEnabled(true);
        user.setUsername(testUserName);
        user.setPassword("test-pwd");
        final User savedUser = userService.save(user);
        assertNotNull(savedUser);

        String id = savedUser.getId();
        System.out.println("\n\n" + savedUser + "\n\n");

        Optional<User> optional = userService.findOne(id);
        optional.ifPresent(storedUser -> {
            logger.debug("Found stored user\n\t" + storedUser + "\n");
            assertEquals(user.getUsername(), storedUser.getUsername());
            assertEquals(user.getPassword(), storedUser.getPassword());
            assertEquals(user.isEnabled(), storedUser.isEnabled());
        });
        optional.orElseThrow(
                () -> new NullPointerException("Unable to locate user " + testUserName + ". Save operation fails..."));
    }

    @Test
    @Transactional
    public synchronized void displayUsers()
    {
        userService.findAll().ifPresent(strongboxUsers -> {
            strongboxUsers.forEach(user -> logger.debug(user.toString()));
        });
    }

}
