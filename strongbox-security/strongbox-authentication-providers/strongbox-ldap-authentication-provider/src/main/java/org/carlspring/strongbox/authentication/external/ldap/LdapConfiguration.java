package org.carlspring.strongbox.authentication.external.ldap;

import org.carlspring.strongbox.authentication.external.ExternalUserProvider;
import org.carlspring.strongbox.authentication.support.AuthoritiesExternalToInternalMapper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.stream.Collectors;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.config.ldap.LdapServerBeanDefinitionParser;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.LdapUtils;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.server.ApacheDSContainer;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "ldap")
@XmlAccessorType(XmlAccessType.NONE)
public class LdapConfiguration
        extends ExternalUserProvider
{

    private static final int DEFAULT_PORT = 33389;

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

    public void setUrl(final String url)
    {
        this.url = url;
    }

    public LdapRolesMapping getRolesMapping()
    {
        return rolesMapping;
    }

    public void setRolesMapping(final LdapRolesMapping rolesMapping)
    {
        this.rolesMapping = rolesMapping;
    }

    public LdapBindAuthenticator getAuthenticator()
    {
        return authenticator;
    }

    public void setAuthenticator(final LdapBindAuthenticator authenticator)
    {
        this.authenticator = authenticator;
    }

    public LdapAuthoritiesPopulator getAuthoritiesPopulator()
    {
        return authoritiesPopulator;
    }

    public void setAuthoritiesPopulator(final LdapAuthoritiesPopulator authoritiesPopulator)
    {
        this.authoritiesPopulator = authoritiesPopulator;
    }

    @Override
    public void registerInApplicationContext(final GenericApplicationContext applicationContext)
    {
        final DefaultSpringSecurityContextSource contextSource;
        if (StringUtils.isNotBlank(ldif))
        {
            contextSource = registerLdapContainer(applicationContext);
        }
        else
        {
            contextSource = new DefaultSpringSecurityContextSource(url);
        }
        contextSource.afterPropertiesSet();
        registerSingleton(applicationContext, contextSource);

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

        AuthoritiesExternalToInternalMapper authoritiesMapper = new AuthoritiesExternalToInternalMapper();
        authoritiesMapper.setRolesMapping(rolesMapping.asMap());
        authoritiesMapper.afterPropertiesSet();
        applicationContext.getParent().getAutowireCapableBeanFactory().autowireBean(authoritiesMapper);
        registerSingleton(applicationContext, authoritiesMapper);

        LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(bindAuthenticator,
                                                                                               defaultLdapAuthoritiesPopulator);
        ldapAuthenticationProvider.setAuthoritiesMapper(authoritiesMapper);


        registerSingleton(applicationContext, ldapAuthenticationProvider);
    }

    private DefaultSpringSecurityContextSource registerLdapContainer(final GenericApplicationContext applicationContext)
    {
        String root = LdapUtils.parseRootDnFromUrl(url);
        String port = getDefaultPort();

        ApacheDSContainer container;
        try
        {
            container = new ApacheDSContainer(root, ldif);
            container.setPort(Integer.valueOf(port));
            container.afterPropertiesSet();
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
        registerSingleton(applicationContext, container);

        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource("ldap://127.0.0.1:" +
                                                                                                  port + "/" + root);
        contextSource.setUserDn("uid=admin,ou=system");
        contextSource.setPassword("secret");

        return contextSource;
    }

    /**
     * @see LdapServerBeanDefinitionParser#getDefaultPort
     */
    private String getDefaultPort()
    {
        ServerSocket serverSocket = null;
        try
        {
            try
            {
                serverSocket = new ServerSocket(DEFAULT_PORT);
            }
            catch (IOException e)
            {
                try
                {
                    serverSocket = new ServerSocket(0);
                }
                catch (IOException e2)
                {
                    return String.valueOf(DEFAULT_PORT);
                }
            }
            return String.valueOf(serverSocket.getLocalPort());
        }
        finally
        {
            if (serverSocket != null)
            {
                try
                {
                    serverSocket.close();
                }
                catch (IOException e)
                {
                }
            }
        }
    }
}
