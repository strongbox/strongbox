package org.carlspring.strongbox.authentication.api.impl.ldap;

import org.carlspring.strongbox.authentication.TestConfig;
import org.carlspring.strongbox.authentication.api.impl.BaseGenericXmlApplicationContextTest;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.users.domain.Privileges;

import java.io.IOException;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Przemyslaw Fusik
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles(profiles = "test")
@Disabled
public class LdapAuthenticationProviderTest
        extends BaseGenericXmlApplicationContextTest
{

    private static final Logger logger = LoggerFactory.getLogger(LdapAuthenticationProviderTest.class);

    @Test
    public void embeddedLdapServerCreationContainsExpectedContextSourceAndData()
            throws Exception
    {
        ContextSource contextSource = appCtx.getBean(ContextSource.class);

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

        LdapAuthenticationProvider LdapAuthenticationProvider = appCtx.getBean(LdapAuthenticationProvider.class);
        Authentication authentication = LdapAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken("przemyslaw.fusik", "password"));

        assertThat(authentication, CoreMatchers.notNullValue());
        assertThat(authentication, CoreMatchers.instanceOf(UsernamePasswordAuthenticationToken.class));
        assertThat(authentication.getPrincipal(), CoreMatchers.instanceOf(LdapUserDetailsImpl.class));
        LdapUserDetails ldapUserDetails = (LdapUserDetailsImpl) authentication.getPrincipal();

        assertThat(ldapUserDetails.getDn(),
                          CoreMatchers.equalTo("uid=przemyslaw.fusik,ou=Users,dc=carlspring,dc=com"));
        assertThat(ldapUserDetails.getPassword(),
                          CoreMatchers.equalTo("password"));
        assertThat(ldapUserDetails.getUsername(),
                          CoreMatchers.equalTo("przemyslaw.fusik"));
        assertThat(authentication.getAuthorities(),
                          CoreMatchers.hasItems(
                                  CoreMatchers.equalTo((GrantedAuthority) Privileges.ADMIN_CREATE_REPO),
                                  CoreMatchers.equalTo((GrantedAuthority) Privileges.ADMIN_DELETE_REPO),
                                  CoreMatchers.equalTo((GrantedAuthority) Privileges.ADMIN_CREATE_REPO),
                                  CoreMatchers.equalTo(new SimpleGrantedAuthority("VIEW_USER")))
        );
    }

    @Test
    public void ldapAuthenticationProviderShouldInvalidedOnWrongPassword()
    {
        assertThrows(BadCredentialsException.class,() -> {
            LdapAuthenticationProvider LdapAuthenticationProvider = appCtx.getBean(LdapAuthenticationProvider.class);
            LdapAuthenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken("przemyslaw.fusik", "not-a-password"));
        });
    }

    @Override
    protected Resource getAuthenticationConfigurationResource()
            throws IOException
    {
        return ConfigurationResourceResolver.getConfigurationResource("strongbox.authentication.providers.xml",
                                                                      "etc/conf/strongbox-authentication-providers.xml");
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }

}
