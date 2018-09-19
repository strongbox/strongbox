package org.carlspring.strongbox.controllers.configuration.security.ldap.support;

import org.carlspring.strongbox.authentication.api.impl.ldap.LdapAuthenticator;
import org.carlspring.strongbox.authentication.external.ExternalUserProviders;
import org.carlspring.strongbox.authentication.external.ExternalUserProvidersFileManager;
import org.carlspring.strongbox.authentication.external.ldap.LdapConfiguration;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationForm;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.stereotype.Component;

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
    private ExternalUserProvidersFileManager externalUserProvidersFileManager;

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
        ExternalUserProviders externalUserProviders = externalUserProvidersFileManager.read();
        if (externalUserProviders == null)
        {
            externalUserProviders = new ExternalUserProviders();
        }
        LdapConfiguration ldapConfiguration;
        if (CollectionUtils.isNotEmpty(externalUserProviders.getProviders()))
        {
            final ExternalUserProviders finalExternalUserProviders = externalUserProviders;
            ldapConfiguration = (LdapConfiguration) externalUserProviders.getProviders()
                                                                         .stream()
                                                                         .filter(p -> LdapConfiguration.class.isAssignableFrom(
                                                                                 p.getClass()))
                                                                         .findFirst()
                                                                         .orElseGet(() ->
                                                                                    {
                                                                                        LdapConfiguration conf = new LdapConfiguration();
                                                                                        finalExternalUserProviders.add(
                                                                                                conf);
                                                                                        return conf;
                                                                                    });
        }
        else
        {
            ldapConfiguration = new LdapConfiguration();
            externalUserProviders.add(ldapConfiguration);
        }


        ldapConfiguration.setUrl(configuration.getUrl());
/*

        TODO

        LdapBindAuthenticator ldapBindAuthenticator = new LdapBindAuthenticator();
        ldapBindAuthenticator.setUserDnPatterns();
        ldapBindAuthenticator.setUserSearch();

        ldapConfiguration.setAuthenticator(ldapBindAuthenticator);

        LdapAuthoritiesPopulator ldapAuthoritiesPopulator = new LdapAuthoritiesPopulator();
        ldapAuthoritiesPopulator.setConvertToUpperCase(configuration.);
        ldapAuthoritiesPopulator.setGroupRoleAttribute();
        ldapAuthoritiesPopulator.setGroupSearchBase();
        ldapAuthoritiesPopulator.setGroupSearchFilter();
        ldapAuthoritiesPopulator.setRolePrefix();
        ldapAuthoritiesPopulator.setSearchSubtree();

        ldapConfiguration.setAuthoritiesPopulator(ldapAuthoritiesPopulator);

        LdapRolesMapping ldapRolesMapping = new LdapRolesMapping();


        configuration.getRolesMapping()
        ldapConfiguration.setRolesMapping();

        externalUserProvidersFileManager.store(externalUserProviders);
        */
    }

    public void dropLdapConfiguration()
    {
        authenticatorsRegistry.drop(LdapAuthenticator.class);
    }
}
