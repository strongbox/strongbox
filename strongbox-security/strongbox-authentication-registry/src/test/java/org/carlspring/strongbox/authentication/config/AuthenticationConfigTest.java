package org.carlspring.strongbox.authentication.config;

import org.carlspring.strongbox.authentication.TestConfig;
import org.carlspring.strongbox.authentication.api.impl.xml.PasswordAuthenticationProvider;
import org.carlspring.strongbox.authentication.registry.AuthenticationProvidersRegistry;

import javax.inject.Inject;
import java.util.Collection;

import com.google.common.collect.Lists;
import org.hamcrest.CoreMatchers;
import org.hamcrest.CustomMatcher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Przemyslaw Fusik
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = TestConfig.class)
public class AuthenticationConfigTest
{

    @Inject
    AuthenticationProvidersRegistry authenticationProvidersRegistry;

    @Test
    public void registryShouldNotBeNull()
    {
        assertThat(getAuthenticationProviderList(), CoreMatchers.notNullValue());
    }

    private Collection<AuthenticationProvider> getAuthenticationProviderList()
    {
        return authenticationProvidersRegistry.getAuthenticationProviderMap().values();
    }

    @Test
    public void registryShouldContainStrongboxBuiltinAuthenticationProvider()
    {
        assertThat(getAuthenticationProviderList(), CoreMatchers.hasItem(
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
        assertThat(Lists.newArrayList(getAuthenticationProviderList()),
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
