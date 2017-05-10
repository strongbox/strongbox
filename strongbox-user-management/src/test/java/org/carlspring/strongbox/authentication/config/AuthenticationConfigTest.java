package org.carlspring.strongbox.authentication.config;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.api.impl.DefaultAuthenticator;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.config.UsersConfig;

import javax.inject.Inject;

import org.hamcrest.CoreMatchers;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { UsersConfig.class,
                                  AuthenticationConfig.class })
public class AuthenticationConfigTest
{

    @Inject
    AuthenticatorsRegistry authenticatorsRegistry;

    @Inject
    ApplicationContext applicationContext;

    @Test
    public void registryShouldNotBeNull()
    {
        assertThat(authenticatorsRegistry, CoreMatchers.notNullValue());
    }

    @Test
    public void registryShouldContainStrongboxBuiltinAuthenticator()
    {
        assertThat(authenticatorsRegistry.getAuthenticators(), Matchers.hasItem(
                new CustomMatcher<Authenticator>("registryShouldContainStrongboxBuiltinAuthenticator")
                {
                    @Override
                    public boolean matches(Object o)
                    {
                        return ((Authenticator) o).getName().equals(
                                DefaultAuthenticator.class.getSimpleName());
                    }
                }));
    }

    @Test
    public void applicationContextShouldContainStrongboxBuiltinAuthenticatorBean()
    {
        assertNotNull(applicationContext.getBean(DefaultAuthenticator.class));
    }


    @Test
    public void registryShouldContainEmptyAuthenticator()
    {
        assertThat(authenticatorsRegistry.getAuthenticators(),
                   Matchers.hasItem(new CustomMatcher<Authenticator>("registryShouldContainEmptyAuthenticator")
                   {
                       @Override
                       public boolean matches(Object o)
                       {
                           return ((Authenticator) o).getName().equals("EmptyAuthenticator");
                       }
                   }));
    }

    @Test
    public void applicationContextShouldContainEmptyAuthenticatorBean()
    {
        assertNotNull(applicationContext.getBean("emptyAuthenticator"));
    }

}