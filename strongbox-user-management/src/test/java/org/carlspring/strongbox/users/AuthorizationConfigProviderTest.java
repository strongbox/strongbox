package org.carlspring.strongbox.users;

import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
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

    @Autowired
    AuthorizationConfigProvider authorizationConfigProvider;

    @Test
    public void testThatParsingWasSuccessful()
    {
        // this is good enough because everything necessary happens inside provider
        // at the bean instantiation stage
        // config will be loaded from db or XML file, going to be validated aso.
        // if optional is present, it means that everything is really ok
        assertTrue(authorizationConfigProvider.getConfig().isPresent());
    }
}
