package org.carlspring.strongbox.controllers.security.ldap.support;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.List;

import org.springframework.security.ldap.authentication.AbstractLdapAuthenticator;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * Use this class if reflection on some other mechanisms is needed to affect LDAP configuration.
 *
 * @author Przemyslaw Fusik
 */
@Component
public class SpringSecurityLdapInternalsUpdater
{

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
}
