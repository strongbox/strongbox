package org.carlspring.strongbox.authentication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.carlspring.strongbox.config.hazelcast.HazelcastConfiguration;
import org.carlspring.strongbox.config.hazelcast.HazelcastInstanceId;
import org.carlspring.strongbox.domain.UserEntry;
import org.carlspring.strongbox.users.dto.User;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.service.impl.EncodedPasswordUser;
import org.carlspring.strongbox.users.service.impl.OrientDbUserService.OrientDb;
import org.carlspring.strongbox.users.service.impl.YamlUserService.Yaml;
import org.carlspring.strongbox.users.userdetails.StrongboxExternalUsersCacheManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles({ "test", "ConfigurableProviderManagerTestConfig" })
@TestPropertySource(properties = { "strongbox.config.file.authentication.providers=classpath:eudst-strongbox-authentication-providers.xml",
                                   "strongbox.authentication.providers.yaml=classpath:/etc/conf/eudst-strongbox-authentication-providers.yaml",
                                   "users.external.cache.seconds=3600",
                                   "strongbox.users.config.yaml=classpath:/etc/conf/eudst-strongbox-security-users.yaml" })
@ContextConfiguration(classes = TestConfig.class)
public class ConfigurableProviderManagerTest
{

    private static final String TEST_USER = "test-user";

    @Value("${users.external.cache.seconds}")
    private int externalUsersInvalidateSeconds;

    @Inject
    @OrientDb
    private UserService orientDbUserService;

    @Inject
    @Yaml
    private UserService yamlUserService;

    @Inject
    private StrongboxExternalUsersCacheManager strongboxUserManager;

    @Inject
    private UserDetailsService userDetailsService;

    @Inject
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    protected EntityManager entityManager;

    @Test
    @Transactional
    public void testExternalUserCache()
    {
        //Check that we have proper implementation of UserDetailsService injected by Spring
        assertTrue(userDetailsService instanceof ConfigurableProviderManager);

        // Check that there is no external user cached
        assertNull(strongboxUserManager.findByUsername(TEST_USER));

        // Load and cache external user
        assertNotNull(userDetailsService.loadUserByUsername(TEST_USER));

        // Check that external user cached
        User externalUser = strongboxUserManager.findByUsername(TEST_USER);
        assertNotNull(externalUser);

        // Check that external user can't be modyfied
        User externalUserToSave = externalUser;
        assertThrows(IllegalStateException.class, () -> orientDbUserService.save(externalUserToSave),
                     () -> "Can't modify external users.");

        // Update external password
        String newPassword = UUID.randomUUID().toString();
        UserDto userToUpdate = new UserDto();
        userToUpdate.setUsername(TEST_USER);
        userToUpdate.setPassword(newPassword);
        yamlUserService.updateAccountDetailsByUsername(new EncodedPasswordUser(userToUpdate, passwordEncoder));

        // Invalidate user cache
        UserEntry externalUserEntry = (UserEntry) orientDbUserService.findByUsername(TEST_USER);
        externalUserEntry.setLastUpdate(null);
        entityManager.persist(externalUserEntry);

        // Update user cahce
        assertNotNull(userDetailsService.loadUserByUsername(TEST_USER));

        // Cached user password should be updated
        externalUser = strongboxUserManager.findByUsername(TEST_USER);
        assertNotNull(externalUser);
        assertTrue(passwordEncoder.matches(newPassword, externalUser.getPassword()));
    }

    @Profile("ConfigurableProviderManagerTestConfig")
    @Import(HazelcastConfiguration.class)
    @Configuration
    public static class ConfigurableProviderManagerTestConfig
    {

        @Primary
        @Bean
        public HazelcastInstanceId hazelcastInstanceIdAcctit()
        {
            return new HazelcastInstanceId("ExternalUserDetailsServiceTest-hazelcast-instance");
        }

    }
}
