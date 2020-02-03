package org.carlspring.strongbox.authentication.api.impl.ldap;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.carlspring.strongbox.authentication.support.AuthenticationContextInitializer;
import org.carlspring.strongbox.config.UsersConfig;
import org.carlspring.strongbox.users.domain.SystemRole;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;

/**
 * @author Przemyslaw Fusik
 * @author sbespalov
 */
@SpringBootTest
@ContextHierarchy({ @ContextConfiguration(classes = UsersConfig.class),
                    @ContextConfiguration(locations = "classpath:/ldapServerApplicationContext.xml"),
                    @ContextConfiguration(initializers = LdapAuthenticationProviderTest.TestContextInitializer.class, locations = "classpath:/org/carlspring/strongbox/authentication/external/ldap/strongbox-authentication-providers.xml") })
@ActiveProfiles(profiles = "test")
public class LdapAuthenticationProviderTest
{

    private static final Logger logger = LoggerFactory.getLogger(LdapAuthenticationProviderTest.class);

    @Inject
    private ContextSource contextSource;

    @Inject
    private LdapUserDetailsService ldapUserDetailsService;

    @Test
    public void embeddedLdapServerCreationContainsExpectedContextSourceAndData()
        throws Exception
    {
        LdapTemplate template = new LdapTemplate(contextSource);
        Object ldapObject = template.lookup("uid=przemyslaw.fusik,ou=Users");

        assertThat(ldapObject).isNotNull();
        assertThat(ldapObject).isInstanceOf(DirContextAdapter.class);
        DirContextAdapter dirContextAdapter = (DirContextAdapter) ldapObject;
        assertThat(dirContextAdapter.getDn().toString()).isEqualTo("uid=przemyslaw.fusik,ou=Users");
        assertThat(dirContextAdapter.getNameInNamespace()).isEqualTo("uid=przemyslaw.fusik,ou=Users,dc=carlspring,dc=com");
    }

    @Test
    public void embeddedLdapServerRegistersExpectedAuthenticationProvider()
    {
        UserDetails ldapUser = ldapUserDetailsService.loadUserByUsername("przemyslaw.fusik");

        assertThat(ldapUser).isInstanceOf(LdapUserDetailsImpl.class);
        LdapUserDetails ldapUserDetails = (LdapUserDetailsImpl) ldapUser;

        assertThat(ldapUserDetails.getDn()).isEqualTo("uid=przemyslaw.fusik,ou=Users,dc=carlspring,dc=com");
        assertThat(ldapUserDetails.getPassword()).isEqualTo("{SHA-256}{mujKRdqeWWYAWhczNwVnBl6L6dHNwWO5eIGZ/G7pnBg=}bb63813f5b6f64ae306ebbbb23dcbb1c6f49eb9b989fc466b1b1a24a011bb2ce");
        assertThat(ldapUserDetails.getUsername()).isEqualTo("przemyslaw.fusik");
        assertThat(((List<SimpleGrantedAuthority>)ldapUser.getAuthorities()))
                .contains(
                        new SimpleGrantedAuthority(SystemRole.REPOSITORY_MANAGER.name()),
                        new SimpleGrantedAuthority("USER_ROLE")
                );
    }

    public static class TestContextInitializer extends AuthenticationContextInitializer
    {

        public TestContextInitializer()
        {
            super(loadPropertySource());
        }

        private static PropertySource<?> loadPropertySource()
        {
            Properties properties = new Properties();
            try (InputStream is = LdapAuthenticationProviderTest.class.getResourceAsStream("lapt.properties"))
            {
                properties.load(is);
            }
            catch (Exception ex)
            {
                throw new UndeclaredThrowableException(ex);
            }

            return new PropertiesPropertySource(STRONGBOX_AUTHENTICATION_PROVIDERS, properties);
        }
    }
}
