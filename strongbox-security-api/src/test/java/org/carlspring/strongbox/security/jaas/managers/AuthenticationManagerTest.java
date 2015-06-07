package org.carlspring.strongbox.security.jaas.managers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class AuthenticationManagerTest
{

    @Autowired
    private AuthenticationManager authenticationManager;


    @Test
    public void testLoad()
            throws Exception
    {
        assertTrue("Failed to load configuration!", authenticationManager.getConfiguration() != null);
        assertTrue("Failed to load realms!", authenticationManager.getRealms() != null);
        assertFalse("Failed to load realms!", authenticationManager.getRealms().isEmpty());
        assertTrue("Failed to load settings for anonymous access!",
                   authenticationManager.getAnonymousAccessConfiguration() != null);
        assertTrue("Failed to load settings for anonymous access!",
                   authenticationManager.getAnonymousAccessConfiguration().isEnabled());
    }

}
