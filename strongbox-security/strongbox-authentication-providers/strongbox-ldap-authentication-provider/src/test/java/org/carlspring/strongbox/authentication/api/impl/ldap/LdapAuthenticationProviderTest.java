package org.carlspring.strongbox.authentication.api.impl.ldap;

import org.carlspring.strongbox.authentication.support.AuthenticationContextInitializer;
import org.carlspring.strongbox.config.UsersConfig;
import org.carlspring.strongbox.users.domain.Privileges;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Properties;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cglib.proxy.UndeclaredThrowableException;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import static org.hamcrest.MatcherAssert.assertThat;

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

        assertThat(ldapObject, CoreMatchers.notNullValue());
        assertThat(ldapObject, CoreMatchers.instanceOf(DirContextAdapter.class));
        DirContextAdapter dirContextAdapter = (DirContextAdapter) ldapObject;
        assertThat(dirContextAdapter.getDn().toString(), CoreMatchers.equalTo("uid=przemyslaw.fusik,ou=Users"));
        assertThat(dirContextAdapter.getNameInNamespace(),
                   CoreMatchers.equalTo("uid=przemyslaw.fusik,ou=Users,dc=carlspring,dc=com"));
    }

    @Test
    public void embeddedLdapServerRegistersExpectedAuthenticationProvider()
    {
        UserDetails ldapUser = ldapUserDetailsService.loadUserByUsername("przemyslaw.fusik");

        assertThat(ldapUser, CoreMatchers.instanceOf(LdapUserDetailsImpl.class));
        LdapUserDetails ldapUserDetails = (LdapUserDetailsImpl) ldapUser;

        assertThat(ldapUserDetails.getDn(), CoreMatchers.equalTo("uid=przemyslaw.fusik,ou=Users,dc=carlspring,dc=com"));
        assertThat(ldapUserDetails.getPassword(), CoreMatchers.equalTo("password"));
        assertThat(ldapUserDetails.getUsername(), CoreMatchers.equalTo("przemyslaw.fusik"));
        assertThat(ldapUser.getAuthorities(),
                   CoreMatchers.hasItems(CoreMatchers.equalTo((GrantedAuthority) Privileges.ADMIN_CREATE_REPO),
                                         CoreMatchers.equalTo((GrantedAuthority) Privileges.ADMIN_DELETE_REPO),
                                         CoreMatchers.equalTo((GrantedAuthority) Privileges.ADMIN_CREATE_REPO),
                                         CoreMatchers.equalTo(new SimpleGrantedAuthority("VIEW_USER"))));
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
