package org.carlspring.strongbox.users;

import org.carlspring.strongbox.users.domain.User;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Alex Oreshkevich
 */
@UserServiceTestContext
@RunWith(SpringJUnit4ClassRunner.class)
public class CacheManagerTest
{

    @Autowired
    CacheManager cacheManager;

    @Test
    public void testThatUsersInCache(){

        System.out.println("All caches size " + cacheManager.getCacheNames().size());

        User user = cacheManager.getCache("users").get("admin", User.class);
        assertNotNull(user);
        assertEquals("admin", user.getUsername());
        assertEquals("password", user.getPassword());

        User maven = cacheManager.getCache("users").get("maven", User.class);
        assertNotNull(maven);
        assertEquals("maven", maven.getUsername());
        assertEquals("password", maven.getPassword());
    }
}
