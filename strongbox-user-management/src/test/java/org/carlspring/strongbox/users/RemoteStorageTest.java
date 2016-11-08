package org.carlspring.strongbox.users;

import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
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
public class RemoteStorageTest
{

    private static final Logger logger = LoggerFactory.getLogger(RemoteStorageTest.class);

    final String testUserName = "TEST";

    @Autowired
    UserService userService;

    @Autowired
    OObjectDatabaseTx databaseTx;

    @Test
    @Transactional
    public synchronized void testSaveOperation()
            throws Exception
    {
        // prepare
        final User user = buildTestUser();

        // if such user already exists, drop it
        try
        {
            User oldUser = userService.findByUserName(user.getUsername());
            if (oldUser != null)
            {
                userService.delete(oldUser.getId());
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            // ignore internal spring-data-orientdb issue
        }

        User user1 = databaseTx.detachAll(userService.save(user), true);

        User storedUser = userService.findOne(user1.getId()).get();
        assertNotNull(storedUser);

        logger.debug("Found user {}", storedUser);

        assertEquals(user.getUsername(), storedUser.getUsername());
        assertEquals(user.getPassword(), storedUser.getPassword());
    }

    private User buildTestUser()
    {
        final User user = new User();
        user.setEnabled(true);
        user.setUsername(testUserName);
        user.setPassword("pwd");

        return user;
    }

}
