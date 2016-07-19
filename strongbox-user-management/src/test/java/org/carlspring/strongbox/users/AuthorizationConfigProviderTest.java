package org.carlspring.strongbox.users;

import org.carlspring.strongbox.users.security.AuthorizationConfig;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * Functional test for {@link org.carlspring.strongbox.users.security.AuthorizationConfigProvider}.
 *
 * @author Alex Oreshkevich
 */
@UserServiceTestContext
@RunWith(SpringJUnit4ClassRunner.class)
public class AuthorizationConfigProviderTest
{

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationConfigProviderTest.class);

    @Autowired
    AuthorizationConfigProvider authorizationConfigProvider;

    @Test
    public void testThatParsingWasSuccessful()
    {
        Optional<AuthorizationConfig> configOptional = authorizationConfigProvider.getConfig();
        assertTrue(configOptional.isPresent());
    }
}
