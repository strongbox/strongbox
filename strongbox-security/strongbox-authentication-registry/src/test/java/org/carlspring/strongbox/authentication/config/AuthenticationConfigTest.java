package org.carlspring.strongbox.authentication.config;

import static org.hamcrest.MatcherAssert.assertThat;

import javax.inject.Inject;

import org.carlspring.strongbox.authentication.TestConfig;
import org.carlspring.strongbox.authentication.api.impl.xml.PasswordAuthenticationProvider;
import org.carlspring.strongbox.authentication.registry.AuthenticationProvidersRegistry;
import org.hamcrest.CoreMatchers;
import org.hamcrest.CustomMatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.common.collect.Lists;

/**
 * @author Przemyslaw Fusik
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = TestConfig.class)
public class AuthenticationConfigTest
{

    @Inject
    AuthenticationProvidersRegistry authenticationProvidersRegistry;

    @Test
    public void registryShouldNotBeNull()
    {
        assertThat(authenticationProvidersRegistry, CoreMatchers.notNullValue());
    }

    @Test
    public void registryShouldContainStrongboxBuiltinAuthenticationProvider()
    {
        assertThat(Lists.newArrayList(authenticationProvidersRegistry), CoreMatchers.hasItem(
                new CustomMatcher<AuthenticationProvider>("registryShouldContainStrongboxBuiltinAuthenticationProvider")
                {
                    @Override
                    public boolean matches(Object o)
                    {
                        return ((AuthenticationProvider) o).getClass().getName()
                                                  .equals(PasswordAuthenticationProvider.class.getName());
                    }
                }));
    }

    @Test
    public void registryShouldContainEmptyAuthenticationProvider()
    {
        assertThat(Lists.newArrayList(authenticationProvidersRegistry),
                   CoreMatchers.hasItem(new CustomMatcher<AuthenticationProvider>("registryShouldContainEmptyAuthenticationProvider")
                   {
                       @Override
                       public boolean matches(Object o)
                       {
                           return ((AuthenticationProvider) o).getClass().getName()
                                                     .equals("org.carlspring.strongbox.authentication.impl.example.EmptyAuthenticationProvider");
                       }
                   }));
    }

}
