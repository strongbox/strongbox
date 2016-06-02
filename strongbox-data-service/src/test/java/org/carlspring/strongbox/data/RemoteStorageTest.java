package org.carlspring.strongbox.data;

import org.carlspring.strongbox.data.domain.StrongboxUser;
import org.carlspring.strongbox.data.repository.StrongboxUserRepository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@DataServiceTestContext
@RunWith(SpringJUnit4ClassRunner.class)
public class RemoteStorageTest
{

    private static final Logger logger = LoggerFactory.getLogger(RemoteStorageTest.class);

    final String testUserName = "TEST";

    @Autowired
    StrongboxUserRepository repository;

    @Test
    @Transactional
    public synchronized void testSaveOperation()
            throws Exception
    {

        // prepare
        final StrongboxUser user = buildTestUser();

        // if such user already exists, drop it
        try
        {
            StrongboxUser oldUser = repository.findByUsername(user.getUsername());
            if (oldUser != null)
            {
                repository.delete(oldUser.getId());
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            // ignore internal spring-data-orientdb issue
        }

        final String id = repository.save(user).getId();

        StrongboxUser storedUser = repository.findOne(id);
        assertNotNull(storedUser);

        logger.warn("Found user {}", storedUser);
        assertEquals(user.getUsername(), storedUser.getUsername());
        assertEquals(user.getPassword(), storedUser.getPassword());
    }

    private StrongboxUser buildTestUser()
    {
        final StrongboxUser user = new StrongboxUser();
        user.setEnabled(true);
        user.setUsername(testUserName);
        user.setPassword("pwd");
        return user;
    }
}
