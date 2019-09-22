package org.carlspring.strongbox.authentication.registry;

import static org.assertj.core.api.Assertions.assertThat;

import org.carlspring.strongbox.authentication.TestConfig;
import org.carlspring.strongbox.authentication.api.password.PasswordAuthenticationProvider;
import org.carlspring.strongbox.config.hazelcast.HazelcastConfiguration;
import org.carlspring.strongbox.config.hazelcast.HazelcastInstanceId;

import javax.inject.Inject;
import java.util.Collection;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * @author Przemyslaw Fusik
 */
@SpringBootTest
@ActiveProfiles({ "test", "AuthenticationProvidersRegistryTestConfig" })
@TestPropertySource(properties = { "strongbox.config.file.authentication.providers=classpath:aprt-strongbox-authentication-providers.xml",
                                   "strongbox.authentication.providers.yaml=classpath:/etc/conf/aprt-strongbox-authentication-providers.yaml" })
@ContextConfiguration(classes = TestConfig.class)
public class AuthenticationProvidersRegistryTest
{

    @Inject
    AuthenticationProvidersRegistry authenticationProvidersRegistry;

    @Test
    public void registryShouldNotBeNull()
    {
        assertThat(getAuthenticationProviderList()).isNotNull();
    }

    private Collection<AuthenticationProvider> getAuthenticationProviderList()
    {
        return authenticationProvidersRegistry.getAuthenticationProviderMap().values();
    }

    @Test
    public void registryShouldContainStrongboxBuiltinAuthenticationProvider()
    {
        assertThat(Lists.newArrayList(getAuthenticationProviderList()))
                .anyMatch(x -> x.getClass().getName().equals(PasswordAuthenticationProvider.class.getName()));
    }

    @Test
    public void registryShouldContainEmptyAuthenticationProvider()
    {
        assertThat(Lists.newArrayList(getAuthenticationProviderList()))
                .anyMatch(x -> x.getClass().getName().equals("org.carlspring.strongbox.authentication.impl.example.EmptyAuthenticationProvider"));
    }
    
    @Profile("AuthenticationProvidersRegistryTestConfig")
    @Import(HazelcastConfiguration.class)
    @Configuration
    public static class AuthenticationProvidersRegistryTestConfig
    {

        @Primary
        @Bean
        public HazelcastInstanceId hazelcastInstanceIdAcctit()
        {
            return new HazelcastInstanceId("AuthenticationProvidersRegistryTest-hazelcast-instance");
        }

    }


}
