package org.carlspring.strongbox.security.managers;

import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.configuration.StrongboxSecurityConfig;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author mtodorov
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration
public class AuthenticationManagerTest
{

    @Configuration
    @Import({ CommonConfig.class,
              StrongboxSecurityConfig.class
              })

    public static class SpringConfig { }

    @Inject
    private XmlAuthenticationConfigurationManager authenticationConfigurationManager;

    @Test
    public void testLoad()
    {
        assertNotNull(authenticationConfigurationManager.getConfiguration(), "Failed to load configuration!");
        assertNotNull(authenticationConfigurationManager.getRealms(), "Failed to load realms!");
        assertFalse(authenticationConfigurationManager.getRealms().isEmpty(), "Failed to load realms!");
        assertNotNull(authenticationConfigurationManager.getAnonymousAccessConfiguration(),
                      "Failed to load settings for anonymous access!");
        assertTrue(authenticationConfigurationManager.getAnonymousAccessConfiguration().isEnabled(),
                   "Failed to load settings for anonymous access!");
    }

}
