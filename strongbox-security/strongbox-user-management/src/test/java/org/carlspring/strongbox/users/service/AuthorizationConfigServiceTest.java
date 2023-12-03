package org.carlspring.strongbox.users.service;

import org.carlspring.strongbox.authorization.service.AuthorizationConfigService;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.UsersConfig;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Functional test for {@link AuthorizationConfigService}
 *
 * @author Alex Oreshkevich
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
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
        // config will be loaded from db or YAML file, going to be validated aso.
        // if optional is present, it means that everything is really ok
        assertThat(authorizationConfigService.get()).isNotNull();
    }
}
