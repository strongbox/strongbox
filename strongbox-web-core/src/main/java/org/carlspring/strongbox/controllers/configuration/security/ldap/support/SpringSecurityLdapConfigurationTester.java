package org.carlspring.strongbox.controllers.configuration.security.ldap.support;

import org.carlspring.strongbox.authentication.support.AuthoritiesExternalToInternalMapper;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationForm;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationTestForm;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapSearchForm;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class SpringSecurityLdapConfigurationTester
        implements ApplicationContextAware
{

    private AutowireCapableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(final ApplicationContext context)
    {
        beanFactory = context.getAutowireCapableBeanFactory();
    }

    public boolean test(final LdapConfigurationTestForm form)
    {
        LdapConfigurationForm configuration = form.getConfiguration();

        ContextSource contextSource = prepareContextSource(configuration);
        LdapAuthenticator ldapAuthenticator = prepareAuthenticator(configuration, contextSource);
        LdapAuthoritiesPopulator ldapAuthoritiesPopulator = prepareAuthoritiesPopulator(configuration, contextSource);

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(ldapAuthenticator,
                                                                             ldapAuthoritiesPopulator);

        if (!CollectionUtils.isEmpty(form.getConfiguration().getRolesMapping()))
        {
            AuthoritiesExternalToInternalMapper authoritiesMapper = new AuthoritiesExternalToInternalMapper();
            beanFactory.autowireBean(authoritiesMapper);
            authoritiesMapper.setRolesMapping(form.getConfiguration().getRolesMapping());
            provider.setAuthoritiesMapper(authoritiesMapper);
        }

        try
        {
            Authentication authentication = provider.authenticate(
                    new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword()));
            return authentication != null && authentication.isAuthenticated();
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    private DefaultLdapAuthoritiesPopulator prepareAuthoritiesPopulator(final LdapConfigurationForm configuration,
                                                                        final ContextSource contextSource)
    {
        LdapSearchForm groupSearch = configuration.getGroupSearch();
        DefaultLdapAuthoritiesPopulator ldapAuthoritiesPopulator = new DefaultLdapAuthoritiesPopulator(contextSource,
                                                                                                       groupSearch.getSearchBase());
        ldapAuthoritiesPopulator.setSearchSubtree(true);
        ldapAuthoritiesPopulator.setGroupSearchFilter(groupSearch.getSearchFilter());
        ldapAuthoritiesPopulator.setRolePrefix(StringUtils.EMPTY);
        ldapAuthoritiesPopulator.setConvertToUpperCase(false);
        return ldapAuthoritiesPopulator;
    }

    private LdapAuthenticator prepareAuthenticator(final LdapConfigurationForm configuration,
                                                   final ContextSource contextSource)
    {
        BaseLdapPathContextSource baseLdapPathContextSource = (BaseLdapPathContextSource) contextSource;
        BindAuthenticator bindAuthenticator = new BindAuthenticator(baseLdapPathContextSource);
        if (configuration.getUserDnPatterns() != null)
        {
            bindAuthenticator.setUserDnPatterns(configuration.getUserDnPatterns().toArray(new String[0]));
        }
        if (configuration.getUserSearch() != null)
        {
            bindAuthenticator.setUserSearch(new FilterBasedLdapUserSearch(configuration.getUserSearch().getSearchBase(),
                                                                          configuration.getUserSearch().getSearchFilter(),
                                                                          baseLdapPathContextSource));
        }
        return bindAuthenticator;
    }

    private ContextSource prepareContextSource(final LdapConfigurationForm configuration)
    {
        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(configuration.getUrl());
        if (StringUtils.isNotEmpty(configuration.getManagerDn()))
        {
            contextSource.setUserDn(configuration.getManagerDn());
        }
        if (StringUtils.isNotEmpty(configuration.getManagerPassword()))
        {
            contextSource.setPassword(configuration.getManagerPassword());
        }
        contextSource.afterPropertiesSet();
        return contextSource;
    }
}
