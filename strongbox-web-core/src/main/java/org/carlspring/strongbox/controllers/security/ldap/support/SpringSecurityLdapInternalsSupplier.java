package org.carlspring.strongbox.controllers.security.ldap.support;

import org.carlspring.strongbox.authentication.api.impl.ldap.LdapAuthenticator;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.authentication.support.AuthoritiesExternalToInternalMapper;
import org.carlspring.strongbox.controllers.security.ldap.support.LdapConstants.LdapMessages;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.ldap.core.ContextSource;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class SpringSecurityLdapInternalsSupplier
{

    @Inject
    private AuthenticatorsRegistry authenticatorsRegistry;

    public boolean isLdapAuthenticationEnabled()
    {
        return StreamSupport.stream(authenticatorsRegistry.spliterator(), false)
                            .filter(a -> a instanceof LdapAuthenticator)
                            .findFirst()
                            .isPresent();
    }

    public LdapAuthenticationProvider getAuthenticationProvider()
    {
        return (LdapAuthenticationProvider) (StreamSupport.stream(authenticatorsRegistry.spliterator(), false)
                                                          .filter(a -> a instanceof LdapAuthenticator)
                                                          .findFirst()
                                                          .orElseThrow(() -> new IllegalStateException(LdapMessages.NOT_CONFIGURED)))
                                                    .getAuthenticationProvider();
    }

    public AbstractLdapAuthenticator getAuthenticator()
    {
        Field authenticator = ReflectionUtils.findField(LdapAuthenticationProvider.class, "authenticator");
        ReflectionUtils.makeAccessible(authenticator);

        return (AbstractLdapAuthenticator) ReflectionUtils.getField(authenticator, getAuthenticationProvider());
    }

    public LdapAuthoritiesPopulator getAuthoritiesPopulator()
    {
        Field authoritiesPopulator = ReflectionUtils.findField(LdapAuthenticationProvider.class,
                                                               "authoritiesPopulator");
        ReflectionUtils.makeAccessible(authoritiesPopulator);

        return (LdapAuthoritiesPopulator) ReflectionUtils.getField(authoritiesPopulator, getAuthenticationProvider());
    }

    public List<String> getUserDnPatterns()
    {
        Field userDnFormat = ReflectionUtils.findField(AbstractLdapAuthenticator.class, "userDnFormat");
        ReflectionUtils.makeAccessible(userDnFormat);
        final MessageFormat[] userDnFormatValue = (MessageFormat[]) ReflectionUtils.getField(userDnFormat,
                                                                                             getAuthenticator());
        return userDnFormatValue == null ? null :
               Stream.of(userDnFormatValue)
                     .map(MessageFormat::toPattern)
                     .collect(Collectors.toList());
    }

    public LdapUserSearch getUserSearch()
    {
        Field userSearch = ReflectionUtils.findField(AbstractLdapAuthenticator.class, "userSearch");
        ReflectionUtils.makeAccessible(userSearch);

        return (LdapUserSearch) ReflectionUtils.getField(userSearch, getAuthenticator());
    }

    public AuthoritiesExternalToInternalMapper getAuthoritiesMapper()
    {
        Field authoritiesMapper = ReflectionUtils.findField(LdapAuthenticationProvider.class, "authoritiesMapper");
        ReflectionUtils.makeAccessible(authoritiesMapper);

        return (AuthoritiesExternalToInternalMapper) ReflectionUtils.getField(authoritiesMapper,
                                                                              getAuthenticationProvider());
    }

    public LdapUserSearchResponseEntityBody getUserSearchXmlHolder(FilterBasedLdapUserSearch userSearch)
    {
        Field searchBase = ReflectionUtils.findField(FilterBasedLdapUserSearch.class, "searchBase");
        ReflectionUtils.makeAccessible(searchBase);
        final String searchBaseValue = (String) ReflectionUtils.getField(searchBase, userSearch);

        Field searchFilter = ReflectionUtils.findField(FilterBasedLdapUserSearch.class, "searchFilter");
        ReflectionUtils.makeAccessible(searchFilter);
        final String searchFilterValue = (String) ReflectionUtils.getField(searchFilter, userSearch);

        return new LdapUserSearchResponseEntityBody().searchBase(searchBaseValue)
                                                     .searchFilter(searchFilterValue);
    }

    public LdapGroupSearchResponseEntityBody ldapGroupSearchHolder(DefaultLdapAuthoritiesPopulator populator)
    {
        Field searchBase = ReflectionUtils.findField(DefaultLdapAuthoritiesPopulator.class, "groupSearchBase");
        ReflectionUtils.makeAccessible(searchBase);
        final String searchBaseValue = (String) ReflectionUtils.getField(searchBase, populator);

        Field searchFilter = ReflectionUtils.findField(DefaultLdapAuthoritiesPopulator.class,
                                                       "groupSearchFilter");
        ReflectionUtils.makeAccessible(searchFilter);
        String searchFilterValue = (String) ReflectionUtils.getField(searchFilter, populator);

        return new LdapGroupSearchResponseEntityBody().searchBase(searchBaseValue)
                                                      .searchFilter(searchFilterValue);
    }

    public ContextSource getContextSource()
    {
        final Field contextSource = ReflectionUtils.findField(AbstractLdapAuthenticator.class, "contextSource");
        ReflectionUtils.makeAccessible(contextSource);

        return (ContextSource) ReflectionUtils.getField(contextSource, getAuthenticator());
    }

}
