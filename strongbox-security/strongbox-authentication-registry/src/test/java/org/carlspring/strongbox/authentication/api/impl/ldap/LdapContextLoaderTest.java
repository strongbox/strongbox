package org.carlspring.strongbox.authentication.api.impl.ldap;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.config.AuthenticationConfig;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.config.UsersConfig;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.users.domain.Privileges;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticator;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ReflectionUtils;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { UsersConfig.class,
                                  AuthenticationConfig.class })
public class LdapContextLoaderTest
{

    private static final Logger logger = LoggerFactory.getLogger(LdapContextLoaderTest.class);

    @Inject
    private AuthenticatorsRegistry authenticatorsRegistry;    

    @Test
    public void embeddedLdapServerCreationContainsExpectedContextSourceAndData() throws Exception
    {
        Method methodGetAuthenticator = LdapAuthenticationProvider.class.getDeclaredMethod("getAuthenticator");
        methodGetAuthenticator.setAccessible(true);
        BindAuthenticator bindAuthenticator = (BindAuthenticator) ReflectionUtils.invokeMethod(methodGetAuthenticator,
                                                                                               getLdapAuthenticationProvider());
        Method methodGetContextSource = AbstractLdapAuthenticator.class.getDeclaredMethod("getContextSource");
        methodGetContextSource.setAccessible(true);
        DefaultSpringSecurityContextSource contextSource = (DefaultSpringSecurityContextSource) ReflectionUtils.invokeMethod(methodGetContextSource,
                                                                                                                             bindAuthenticator);

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

        LdapAuthenticationProvider LdapAuthenticationProvider = getLdapAuthenticationProvider();
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
        try
        {
            Class<?> loadClass = Thread.currentThread().getContextClassLoader().loadClass("org.carlspring.strongbox.authentication.api.impl.DefaultAuthenticator");
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        LdapAuthenticationProvider LdapAuthenticationProvider = getLdapAuthenticationProvider();
        Authentication authentication = LdapAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken("przemyslaw.fusik", "not-a-password"));
    }

    protected LdapAuthenticationProvider getLdapAuthenticationProvider()
    {
        Iterator<Authenticator> i = authenticatorsRegistry.iterator();
        i.next();
        i.next();
        LdapAuthenticationProvider LdapAuthenticationProvider = (org.springframework.security.ldap.authentication.LdapAuthenticationProvider) i.next().getAuthenticationProvider();
        return LdapAuthenticationProvider;
    }

    @Test
    public void registryShouldContainLdapAuthenticator()
    {
        Assert.assertTrue(StreamSupport.stream(authenticatorsRegistry.spliterator(), false).filter(
                authenticator -> authenticator instanceof LdapAuthenticator).findFirst().isPresent());
    }

    private Resource getAuthenticationConfigurationResource()
            throws IOException
    {
        return ConfigurationResourceResolver.getConfigurationResource("authentication.providers.xml",
                                                                      "etc/conf/strongbox-authentication-providers.xml");
    }

}
