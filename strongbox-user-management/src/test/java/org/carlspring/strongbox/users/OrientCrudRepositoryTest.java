package org.carlspring.strongbox.users;

import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;

import java.util.Optional;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

@UserServiceTestContext
@RunWith(SpringJUnit4ClassRunner.class)
public class OrientCrudRepositoryTest
{

    private static final Logger logger = LoggerFactory.getLogger(OrientCrudRepositoryTest.class);

    final String testUserName = "TEST";

    @Autowired
    UserService userService;

    @Autowired
    OObjectDatabaseTx databaseTx;

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
        user.setUsername(testUserName);
        user.setPassword("test-pwd");
        assertNull(user.getObjectId());

        final User storedUser = userService.save(user);
        assertNotNull(storedUser);
        String id = storedUser.getObjectId();
        assertNotNull(id);

        logger.debug("Saved user: " + storedUser);

        Optional<User> optional = userService.findOne(id);
        optional.ifPresent(foundEntity ->
                           {
                               logger.debug("Found stored user\n\t" + foundEntity + "\n");
                               assertEquals(storedUser.getObjectId(), foundEntity.getObjectId());
                               assertEquals(storedUser.getUsername(), foundEntity.getUsername());
                               assertEquals(storedUser.getPassword(), foundEntity.getPassword());
                               assertEquals(storedUser.isEnabled(), foundEntity.isEnabled());
        });
        optional.orElseThrow(
                () -> new NullPointerException("Unable to locate user " + testUserName + ". Save operation fails..."));
    }

    @Test
    public void displayUsers()
    {
        userService.findAll()
                   .ifPresent(strongboxUsers -> strongboxUsers.forEach(user -> logger.debug(user.toString())));
    }

}
