package org.carlspring.strongbox.controllers.configuration.security.ldap.support;

import org.carlspring.strongbox.authentication.api.impl.ldap.LdapAuthenticator;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationForm;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.List;

import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * Use this class if reflection on some other mechanisms is needed to affect LDAP configuration.
 *
 * @author Przemyslaw Fusik
 */
@Component
public class SpringSecurityLdapInternalsMutator
        extends LdapAuthenticationProviderCreator
{

    @Inject
    private AuthenticatorsRegistry authenticatorsRegistry;

    @Inject
    private SpringSecurityLdapInternalsSupplier springSecurityLdapInternalsSupplier;

    public void updateGroupSearchFilter(DefaultLdapAuthoritiesPopulator ldapAuthoritiesPopulator,
                                        String searchBase,
                                        String searchFilter)
    {
        ldapAuthoritiesPopulator.setGroupSearchFilter(searchFilter);
        Field groupSearchBase = ReflectionUtils.findField(DefaultLdapAuthoritiesPopulator.class, "groupSearchBase");
        ReflectionUtils.makeAccessible(groupSearchBase);
        ReflectionUtils.setField(groupSearchBase, ldapAuthoritiesPopulator, searchBase);
    }

    public void updateUserDnPatterns(List<String> userDnPatterns)
    {
        final AbstractLdapAuthenticator authenticator = springSecurityLdapInternalsSupplier.getAuthenticator();
        authenticator.setUserDnPatterns(userDnPatterns.toArray(new String[0]));
    }

    public void updateUserSearchFilter(final AbstractLdapAuthenticator abstractLdapAuthenticator,
                                       final String searchBase,
                                       final String searchFilter)
    {
        abstractLdapAuthenticator.setUserSearch(new FilterBasedLdapUserSearch(searchBase, searchFilter,
                                                                              (BaseLdapPathContextSource) springSecurityLdapInternalsSupplier.getContextSource()));
    }

    public void saveLdapConfiguration(LdapConfigurationForm configuration)
    {
        LdapAuthenticationProvider provider = createProvider(configuration);
        authenticatorsRegistry.put(new LdapAuthenticator()
        {
            @Nonnull
            @Override
            public AuthenticationProvider getAuthenticationProvider()
            {
                return provider;
            }
        });
    }

    public void dropLdapConfiguration()
    {
        authenticatorsRegistry.drop(LdapAuthenticator.class);
    }
}
