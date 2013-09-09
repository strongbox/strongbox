package org.carlspring.strongbox.jaas.caching;

import org.carlspring.strongbox.jaas.Credentials;
import org.carlspring.strongbox.jaas.User;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author mtodorov
 */
public class CachedUserManagerTest
{

    @Test
    public void testCredentialsManagement()
            throws Exception
    {
        CachedUserManager manager = new CachedUserManager();
        manager.setCredentialsLifetime(5000l);
        manager.setCredentialExpiredCheckInterval(500l);

        manager.addUser(new User("user1", new Credentials("password")));
        manager.addUser(new User("user2", new Credentials("password")));
        manager.addUser(new User("user3", new Credentials("password")));
        manager.addUser(new User("user4", new Credentials("password")));
        manager.addUser(new User("user5", new Credentials("password")));

        Thread.sleep(3000l);

        manager.validCredentials("user3", "password");
        manager.validCredentials("user5", "password");

        Thread.sleep(4000);

        assertEquals("Failed to expire users from cache!", 2, manager.getSize());
    }

}
