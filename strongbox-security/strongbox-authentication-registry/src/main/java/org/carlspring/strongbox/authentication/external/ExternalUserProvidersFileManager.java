package org.carlspring.strongbox.authentication.external;

import org.carlspring.strongbox.xml.XmlFileManager;

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
public class ExternalUserProvidersFileManager
        extends XmlFileManager<ExternalUserProviders>
{

    public ExternalUserProvidersFileManager()
    {
        super(ExternalUserProvider.class);
    }

    @Override
    public String getPropertyKey()
    {
        return "strongbox.external.user.providers.xml";
    }

    @Override
    public String getDefaultLocation()
    {
        return "etc/conf/strongbox-external-user-providers.xml";
    }

    @Override
    public ExternalUserProviders read()
    {
        // TODO save
        return super.read();
    }

}
