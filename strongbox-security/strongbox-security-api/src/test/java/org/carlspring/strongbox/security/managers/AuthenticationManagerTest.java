package org.carlspring.strongbox.security.managers;

import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.configuration.StrongboxSecurityConfig;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author mtodorov
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class AuthenticationManagerTest
{
    @Configuration
    @Import({
            StrongboxSecurityConfig.class,
            CommonConfig.class
    })
    public static class SpringConfig { }

    @Inject
    private AuthenticationManager authenticationManager;

    @Test
    public void testLoad()
            throws Exception
    {
        assertTrue(authenticationManager.getConfiguration() != null, "Failed to load configuration!");
        assertTrue(authenticationManager.getRealms() != null, "Failed to load realms!");
        assertFalse(authenticationManager.getRealms().isEmpty(), "Failed to load realms!");
        assertTrue(authenticationManager.getAnonymousAccessConfiguration() != null,
                   "Failed to load settings for anonymous access!");
        assertTrue(authenticationManager.getAnonymousAccessConfiguration().isEnabled(),
                   "Failed to load settings for anonymous access!");
    }

}
