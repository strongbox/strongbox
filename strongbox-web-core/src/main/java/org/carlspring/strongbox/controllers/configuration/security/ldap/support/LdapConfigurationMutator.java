package org.carlspring.strongbox.controllers.configuration.security.ldap.support;

import org.carlspring.strongbox.authentication.api.impl.ldap.LdapAuthenticator;
import org.carlspring.strongbox.authentication.external.ExternalUserProvider;
import org.carlspring.strongbox.authentication.external.ExternalUserProviders;
import org.carlspring.strongbox.authentication.external.ExternalUserProvidersFileManager;
import org.carlspring.strongbox.authentication.external.ldap.*;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationForm;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapUserSearchForm;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Use this class if reflection on some other mechanisms is needed to affect LDAP configuration.
 *
 * @author Przemyslaw Fusik
 */
@Component
public class LdapConfigurationMutator
        extends LdapAuthenticationProviderCreator
{

    @Inject
    private AuthenticatorsRegistry authenticatorsRegistry;

    @Inject
    private ExternalUserProvidersFileManager externalUserProvidersFileManager;

    public void dropLdapConfiguration()
    {
        authenticatorsRegistry.drop(LdapAuthenticator.class);
        ExternalUserProviders externalUserProviders = getExternalUserProviders();
        dropLdapConfiguration(externalUserProviders);
    }

    private void dropLdapConfiguration(final ExternalUserProviders externalUserProviders)
    {
        if (!CollectionUtils.isEmpty(externalUserProviders.getProviders()))
        {
            for (Iterator<ExternalUserProvider> it = externalUserProviders.getProviders().iterator(); it.hasNext(); )
            {
                ExternalUserProvider provider = it.next();
                if (provider instanceof LdapConfiguration)
                {
                    it.remove();
                }
            }
        }
    }

    public void saveLdapConfiguration(LdapConfigurationForm configuration)
    {
        overrideAuthenticator(configuration);
        overrideConfiguration(configuration);
    }

    private void overrideConfiguration(final LdapConfigurationForm configuration)
    {
        ExternalUserProviders externalUserProviders = getExternalUserProviders();
        LdapConfiguration ldapConfiguration = getLdapConfiguration(externalUserProviders);

        ldapConfiguration.setUrl(configuration.getUrl());

        setBindAuthenticator(configuration, ldapConfiguration);
        setAuthoritiesPopulator(configuration, ldapConfiguration);
        setRolesMapping(configuration, ldapConfiguration);

        externalUserProvidersFileManager.store(externalUserProviders);
    }

    private void setRolesMapping(final LdapConfigurationForm configuration,
                                 final LdapConfiguration ldapConfiguration)
    {
        ldapConfiguration.setRolesMapping(mapRolesMapping(configuration.getRolesMapping()));
    }

    private void setBindAuthenticator(final LdapConfigurationForm configuration,
                                      final LdapConfiguration ldapConfiguration)
    {
        LdapBindAuthenticator ldapBindAuthenticator = new LdapBindAuthenticator();
        ldapBindAuthenticator.setUserDnPatterns(mapUserDnPatterns(configuration.getUserDnPatterns()));
        ldapBindAuthenticator.setUserSearch(mapUserSearch(configuration.getUserSearch()));
        ldapConfiguration.setAuthenticator(ldapBindAuthenticator);
    }

    private void setAuthoritiesPopulator(final LdapConfigurationForm configuration,
                                         final LdapConfiguration ldapConfiguration)
    {
        LdapAuthoritiesPopulator ldapAuthoritiesPopulator = new LdapAuthoritiesPopulator();
        String groupRoleAttr = configuration.getGroupSearch().getGroupRoleAttribute();
        if (StringUtils.isNotBlank(groupRoleAttr))
        {
            ldapAuthoritiesPopulator.setGroupRoleAttribute(groupRoleAttr);
        }

        ldapAuthoritiesPopulator.setGroupSearchBase(configuration.getGroupSearch().getSearchBase());
        ldapAuthoritiesPopulator.setGroupSearchFilter(configuration.getGroupSearch().getSearchFilter());
        ldapConfiguration.setAuthoritiesPopulator(ldapAuthoritiesPopulator);
    }

    private LdapConfiguration getLdapConfiguration(final ExternalUserProviders externalUserProviders)
    {
        Optional<LdapConfiguration> ldapConfiguration = Optional.empty();
        if (!CollectionUtils.isEmpty(externalUserProviders.getProviders()))
        {
            ldapConfiguration = externalUserProviders.getProviders()
                                                     .stream()
                                                     .filter(p -> LdapConfiguration.class.isAssignableFrom(
                                                             p.getClass()))
                                                     .map(provider -> (LdapConfiguration) provider)
                                                     .findFirst();
        }

        return ldapConfiguration.orElseGet(() ->
                                           {
                                               LdapConfiguration conf = new LdapConfiguration();
                                               externalUserProviders.add(conf);
                                               return conf;
                                           });
    }

    private ExternalUserProviders getExternalUserProviders()
    {
        ExternalUserProviders externalUserProviders = externalUserProvidersFileManager.read();
        if (externalUserProviders == null)
        {
            externalUserProviders = new ExternalUserProviders();
        }
        return externalUserProviders;
    }

    private void overrideAuthenticator(final LdapConfigurationForm configuration)
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

    private LdapRolesMapping mapRolesMapping(Map<String, String> rolesMapping)
    {
        if (CollectionUtils.isEmpty(rolesMapping))
        {
            return null;
        }
        LdapRolesMapping target = new LdapRolesMapping();
        for (Map.Entry<String, String> entry : rolesMapping.entrySet())
        {
            LdapRoleMapping mapping = new LdapRoleMapping();
            mapping.setLdapRole(entry.getKey());
            mapping.setStrongboxRole(entry.getValue());
        }
        return target;
    }

    private LdapUserSearch mapUserSearch(LdapUserSearchForm searchForm)
    {
        LdapUserSearch target = new LdapUserSearch();
        target.setSearchBase(searchForm.getSearchBase());
        target.setSearchFilter(searchForm.getSearchFilter());
        return target;
    }

    private LdapUserDnPatterns mapUserDnPatterns(List<String> userDnPatterns)
    {
        if (CollectionUtils.isEmpty(userDnPatterns))
        {
            return null;
        }
        LdapUserDnPatterns target = new LdapUserDnPatterns();
        userDnPatterns.stream()
                      .map(pattern ->
                           {
                               LdapUserDnPattern result = new LdapUserDnPattern();
                               result.setValue(pattern);
                               return result;
                           })
                      .forEach(pattern -> target.add(pattern));
        return target;
    }


}
