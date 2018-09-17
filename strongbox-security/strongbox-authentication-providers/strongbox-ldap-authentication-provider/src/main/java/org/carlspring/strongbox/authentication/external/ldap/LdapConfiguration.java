package org.carlspring.strongbox.authentication.external.ldap;

import org.carlspring.strongbox.authentication.external.ExternalUserProvider;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.stream.Collectors;

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "ldap")
@XmlAccessorType(XmlAccessType.NONE)
public class LdapConfiguration
        extends ExternalUserProvider
{

    @XmlAttribute(required = true)
    private String url;

    /**
     * For embedded server. Should not be used in production.
     */
    @XmlAttribute
    private String ldif;

    @XmlElement(name = "roles-mapping")
    private LdapRolesMapping rolesMapping;

    @XmlElement(required = true)
    private LdapBindAuthenticator authenticator;

    @XmlElement(name = "authorities-populator")
    private LdapAuthoritiesPopulator authoritiesPopulator;

    public String getUrl()
    {
        return url;
    }

    public String getLdif()
    {
        return ldif;
    }

    public LdapBindAuthenticator getAuthenticator()
    {
        return authenticator;
    }

    public LdapAuthoritiesPopulator getAuthoritiesPopulator()
    {
        return authoritiesPopulator;
    }

    @Override
    public void appContext(final GenericApplicationContext applicationContext)
    {
        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(url);
        BindAuthenticator bindAuthenticator = new BindAuthenticator(contextSource);
        if (authenticator.getUserSearch() != null)
        {
            bindAuthenticator.setUserSearch(new FilterBasedLdapUserSearch(authenticator.getUserSearch().getSearchBase(),
                                                                          authenticator.getUserSearch().getSearchFilter(),
                                                                          contextSource));
        }
        if (authenticator.getUserDnPatterns() != null)
        {
            bindAuthenticator.setUserDnPatterns(authenticator.getUserDnPatterns()
                                                             .getUserDnPattern()
                                                             .stream()
                                                             .map(p -> p.getValue())
                                                             .collect(Collectors.toList())
                                                             .toArray(new String[0]));
        }

        DefaultLdapAuthoritiesPopulator defaultLdapAuthoritiesPopulator = new DefaultLdapAuthoritiesPopulator(contextSource,
                                                                                                              authoritiesPopulator.getGroupSearchBase());

        defaultLdapAuthoritiesPopulator.setGroupSearchFilter(authoritiesPopulator.getGroupSearchFilter());
        defaultLdapAuthoritiesPopulator.setConvertToUpperCase(authoritiesPopulator.isConvertToUpperCase());
        defaultLdapAuthoritiesPopulator.setGroupRoleAttribute(authoritiesPopulator.getGroupRoleAttribute());
        defaultLdapAuthoritiesPopulator.setRolePrefix(authoritiesPopulator.getRolePrefix());
        defaultLdapAuthoritiesPopulator.setSearchSubtree(authoritiesPopulator.isSearchSubtree());

        LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(bindAuthenticator,
                                                                                               defaultLdapAuthoritiesPopulator);
        registerSingleton(applicationContext, ldapAuthenticationProvider);
    }
}
