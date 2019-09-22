package org.carlspring.strongbox.authentication;

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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
        assertThat(userDetailsService instanceof ConfigurableProviderManager).isTrue();

        // Check that there is no external user cached
        assertThat(strongboxUserManager.findByUsername(TEST_USER)).isNull();

        // Load and cache external user
        assertThat(userDetailsService.loadUserByUsername(TEST_USER)).isNotNull();

        // Check that external user cached
        User externalUser = strongboxUserManager.findByUsername(TEST_USER);
        assertThat(externalUser).isNotNull();

        // Check that external user can't be modyfied
        User externalUserToSave = externalUser;
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> orientDbUserService.save(externalUserToSave))
                .withMessageMatching("Can't modify external users.");

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
        assertThat(userDetailsService.loadUserByUsername(TEST_USER)).isNotNull();

        // Cached user password should be updated
        externalUser = strongboxUserManager.findByUsername(TEST_USER);
        assertThat(externalUser).isNotNull();
        assertThat(passwordEncoder.matches(newPassword, externalUser.getPassword())).isTrue();
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
