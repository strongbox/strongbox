package org.carlspring.strongbox.users;

import org.carlspring.strongbox.security.Role;
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
        // this is good enough because everything necessary happens inside provider
        // at the bean instantiation stage
        // config will be loaded from db or XML file, going to be validated aso.
        // if optional is present, it means that everything is really ok
        assertTrue(authorizationConfigProvider.getConfig().isPresent());
    }

    @Test
    public void testAccessToEmbeddedEntities()
            throws Exception
    {

        Optional<AuthorizationConfig> configOptional = authorizationConfigProvider.getConfig();
        configOptional.orElseThrow(() -> new RuntimeException("Unable to load config"));
        configOptional.ifPresent(this::displayEmbeddedRoles);
    }

    @Test
    public void testUpdateAndAccessToEmbeddedEntities()
            throws Exception
    {
        Optional<AuthorizationConfig> configOptional = authorizationConfigProvider.getConfig();
        configOptional.orElseThrow(() -> new RuntimeException("Unable to load config"));
        configOptional.ifPresent(authorizationConfig ->
                                 {
                                     // update config: add new role
                                     Role testRole = new Role();
                                     testRole.setName("MY_ROLE");
                                     testRole.getPrivileges().add("MY_PRIVILEGE");
                                     authorizationConfig.getRoles().getRoles().add(testRole);
                                     authorizationConfigProvider.updateConfig(authorizationConfig);

                                     // retrieve config again and display embedded properties
                                     authorizationConfigProvider.getConfig().ifPresent(this::displayEmbeddedRoles);
                                 });
    }

    private void displayEmbeddedRoles(AuthorizationConfig authorizationConfig)
    {
        // iterate over all roles and print every name
        // (simulates access to embedded entity and it's properties)
        authorizationConfig.getRoles().getRoles().forEach(role ->
                                                          {
                                                              logger.debug("Role name is " + role.getName());
                                                              logger.debug("Privileges " + role.getPrivileges());
                                                          });
    }
}
