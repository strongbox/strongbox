package org.carlspring.strongbox.authentication.api.impl.ldap;

import org.carlspring.strongbox.authentication.config.AuthenticationConfig;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.config.UsersConfig;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.users.domain.Privileges;

import javax.inject.Inject;
import java.io.IOException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { UsersConfig.class,
                                  AuthenticationConfig.class })
public class LdapContextLoaderTest
{

    private static final Logger logger = LoggerFactory.getLogger(LdapContextLoaderTest.class);


    GenericXmlApplicationContext appCtx;

    @Inject
    private ApplicationContext parentApplicationContext;

    @Before
    public void initAppContext()
            throws IOException
    {
        if (appCtx == null)
        {
            appCtx = new GenericXmlApplicationContext();

            appCtx.setParent(parentApplicationContext);
            appCtx.load(getAuthenticationConfigurationResource());
            appCtx.refresh();
        }
    }

    @After
    public void closeAppContext()
    {
        if (appCtx != null)
        {
            appCtx.close();
            appCtx = null;
        }
    }


    private void displayBeanNames()
    {
        if (appCtx != null)
        {
            Stream.of(appCtx.getBeanDefinitionNames())
                  .forEach(logger::debug);
        }
    }

    @Test
    public void embeddedLdapServerCreationContainsExpectedContextSourceAndData()
    {

        DefaultSpringSecurityContextSource contextSource = (DefaultSpringSecurityContextSource) appCtx.getBean(
                "contextSource");

        LdapTemplate template = new LdapTemplate(contextSource);
        Object ldapObject = template.lookup("uid=przemyslaw.fusik,ou=Users");

        Assert.assertThat(ldapObject, CoreMatchers.notNullValue());
        Assert.assertThat(ldapObject, CoreMatchers.instanceOf(DirContextAdapter.class));
        DirContextAdapter dirContextAdapter = (DirContextAdapter) ldapObject;
        Assert.assertThat(dirContextAdapter.getDn().toString(), CoreMatchers.equalTo("uid=przemyslaw.fusik,ou=Users"));
        Assert.assertThat(dirContextAdapter.getNameInNamespace(),
                          CoreMatchers.equalTo("uid=przemyslaw.fusik,ou=Users,dc=carlspring,dc=com"));
    }

    @Test
    public void embeddedLdapServerRegistersExpectedAuthenticationProvider()
    {

        LdapAuthenticationProvider LdapAuthenticationProvider = (org.springframework.security.ldap.authentication.LdapAuthenticationProvider) appCtx.getBean(
                "ldapAuthenticationProvider");
        Authentication authentication = LdapAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken("przemyslaw.fusik", "password"));

        Assert.assertThat(authentication, CoreMatchers.notNullValue());
        Assert.assertThat(authentication, CoreMatchers.instanceOf(UsernamePasswordAuthenticationToken.class));
        Assert.assertThat(authentication.getPrincipal(), CoreMatchers.instanceOf(LdapUserDetailsImpl.class));
        LdapUserDetails ldapUserDetails = (LdapUserDetailsImpl) authentication.getPrincipal();

        Assert.assertThat(ldapUserDetails.getDn(),
                          CoreMatchers.equalTo("uid=przemyslaw.fusik,ou=Users,dc=carlspring,dc=com"));
        Assert.assertThat(ldapUserDetails.getPassword(),
                          CoreMatchers.equalTo("password"));
        Assert.assertThat(ldapUserDetails.getUsername(),
                          CoreMatchers.equalTo("przemyslaw.fusik"));
        Assert.assertThat(ldapUserDetails.getAuthorities(),
                          CoreMatchers.hasItems(
                                  CoreMatchers.equalTo((GrantedAuthority) Privileges.ADMIN_CREATE_REPO),
                                  CoreMatchers.equalTo((GrantedAuthority) Privileges.ADMIN_DELETE_REPO),
                                  CoreMatchers.equalTo((GrantedAuthority) Privileges.ADMIN_CREATE_REPO),
                                  CoreMatchers.equalTo(new SimpleGrantedAuthority("VIEW_USER")))
        );
    }

    @Test(expected = BadCredentialsException.class)
    public void ldapAuthenticationProviderShouldInvalidedOnWrongPassword()
    {

        LdapAuthenticationProvider LdapAuthenticationProvider = appCtx.getBean(LdapAuthenticationProvider.class);
        Authentication authentication = LdapAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken("przemyslaw.fusik", "not-a-password"));
    }

    @Test
    public void registryShouldContainLdapAuthenticator()
    {
        AuthenticatorsRegistry registry = appCtx.getBean(AuthenticatorsRegistry.class);
        Assert.assertTrue(StreamSupport.stream(registry.spliterator(), false).filter(
                authenticator -> authenticator instanceof LdapAuthenticator).findFirst().isPresent());
    }

    private Resource getAuthenticationConfigurationResource()
            throws IOException
    {
        return ConfigurationResourceResolver.getConfigurationResource("authentication.providers.xml",
                                                                      "etc/conf/strongbox-authentication-providers.xml");
    }

}
