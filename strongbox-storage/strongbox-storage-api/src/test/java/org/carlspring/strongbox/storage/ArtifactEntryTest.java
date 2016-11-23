package org.carlspring.strongbox.storage;

import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.services.ArtifactEntryService;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.assertNotNull;

@ArtifactEntryServiceTestContext
@RunWith(SpringJUnit4ClassRunner.class)
public class ArtifactEntryTest
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEntryTest.class);

    final String testUserName = "TEST";

    @Autowired
    ArtifactEntryService a;

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
            User oldUser = userService.findByUsername(user.getUsername());
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
