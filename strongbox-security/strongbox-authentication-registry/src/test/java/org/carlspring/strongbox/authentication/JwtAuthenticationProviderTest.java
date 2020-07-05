package org.carlspring.strongbox.authentication;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.inject.Inject;

import org.carlspring.strongbox.authentication.api.jwt.JwtAuthentication;
import org.carlspring.strongbox.config.hazelcast.HazelcastConfiguration;
import org.carlspring.strongbox.config.hazelcast.HazelcastInstanceId;
import org.carlspring.strongbox.domain.UserEntity;
import org.carlspring.strongbox.domain.SecurityRoleEntity;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.security.JwtAuthenticationClaimsProvider;
import org.carlspring.strongbox.users.security.JwtClaimsProvider;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.service.impl.EncodedPasswordUser;
import org.carlspring.strongbox.users.service.impl.DatabaseUserService;
import org.carlspring.strongbox.users.service.impl.DatabaseUserService.Database;
import org.carlspring.strongbox.users.service.impl.YamlUserService.Yaml;
import org.carlspring.strongbox.users.userdetails.SpringSecurityUser;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles({ "test", "JwtAuthenticationProviderTestConfig" })
@TestPropertySource(properties = { "strongbox.config.file.authentication.providers=classpath:japt-strongbox-authentication-providers.xml",
                                   "strongbox.authentication.providers.yaml=classpath:/etc/conf/japt-strongbox-authentication-providers.yaml",
                                   "users.external.cache.seconds=1",
                                   "strongbox.users.config.yaml=classpath:/etc/conf/japt-strongbox-security-users.yaml" })
@ContextConfiguration(classes = TestConfig.class)
public class JwtAuthenticationProviderTest
{

    private static final String TEST_USER = "test-user";

    @Inject
    private AuthenticationManager authenticationManager;

    @Inject
    private SecurityTokenProvider securityTokenProvider;

    @Inject
    @JwtAuthenticationClaimsProvider.JwtAuthentication
    private JwtClaimsProvider jwtClaimsProvider;

    @Inject
    private UserDetailsService userDetailsService;

    @Inject
    @Yaml
    private UserService userService;

    @Inject
    private DatabaseUserService databaseUserService;
    
    @Inject
    private PasswordEncoder passwordEncoder;

    @Test
    public void testUserHash()
        throws Exception
    {
        SpringSecurityUser userDetails = (SpringSecurityUser) userDetailsService.loadUserByUsername(TEST_USER);
        String token = securityTokenProvider.getToken(TEST_USER, jwtClaimsProvider.getClaims(userDetails), 3600, null);
        JwtAuthentication authentication = new JwtAuthentication(TEST_USER, token);

        //Authentication should pass with valid token
        authenticationManager.authenticate(authentication);

        //Change user password
        UserDto user = new UserDto();
        user.setUsername(TEST_USER);
        user.setPassword("new_password");
        userService.updateAccountDetailsByUsername(new EncodedPasswordUser(user, passwordEncoder));
        
        databaseUserService.expireUser(TEST_USER, false);
        
        //Authentication should fail by token hash
        assertThrows(BadCredentialsException.class, new Authenticate(authentication)::execute);
        
        //Authentication should pass with valid token
        authenticationManager.authenticate(authentication = getAuthentication(TEST_USER));
        
        //Change roles
        UserEntity userEntity = databaseUserService.findByUsername(TEST_USER);
        userEntity.getRoles().add(new SecurityRoleEntity("LOGS_MANAGER"));
        userService.save(userEntity);
        
        databaseUserService.expireUser(TEST_USER, false);
        
        //Authentication should fail by token hash
        assertThrows(BadCredentialsException.class, new Authenticate(authentication)::execute);
        
        //Authentication should pass with valid token
        authenticationManager.authenticate(getAuthentication(TEST_USER));
    }

    protected JwtAuthentication getAuthentication(String username)
        throws JoseException
    {
        SpringSecurityUser  userDetails = (SpringSecurityUser) userDetailsService.loadUserByUsername(username);
        String token = securityTokenProvider.getToken(TEST_USER, jwtClaimsProvider.getClaims(userDetails), 3600, null);
        return  new JwtAuthentication(TEST_USER, token);
    }

    @Profile("JwtAuthenticationProviderTestConfig")
    @Import(HazelcastConfiguration.class)
    @Configuration
    public static class JwtAuthenticationProviderTestConfig
    {

        @Primary
        @Bean
        public HazelcastInstanceId hazelcastInstanceIdAcctit()
        {
            return new HazelcastInstanceId("JwtAuthenticationProviderTest-hazelcast-instance");
        }

    }

    private class Authenticate implements Executable {
        
        private final Authentication authentication;

        public Authenticate(Authentication authentication)
        {
            this.authentication = authentication;
        }

        @Override
        public void execute()
            throws Throwable
        {
            authenticationManager.authenticate(authentication);
        }
        
    }
}
