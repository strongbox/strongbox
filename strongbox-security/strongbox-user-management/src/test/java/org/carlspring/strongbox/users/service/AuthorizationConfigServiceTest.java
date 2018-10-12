package org.carlspring.strongbox.users.service;

import org.carlspring.strongbox.authorization.service.AuthorizationConfigService;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.UsersConfig;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Functional test for {@link AuthorizationConfigService}
 *
 * @author Alex Oreshkevich
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { DataServiceConfig.class,
                                  UsersConfig.class })
public class AuthorizationConfigServiceTest
{
    @Inject
    AuthorizationConfigService authorizationConfigService;

    @Test
    public void testThatParsingWasSuccessful()
    {
        // this is good enough because everything necessary happens inside provider
        // at the bean instantiation stage
        // config will be loaded from db or XML file, going to be validated aso.
        // if optional is present, it means that everything is really ok
        assertNotNull(authorizationConfigService.get());
    }
}
