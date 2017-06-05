package org.carlspring.strongbox.authentication.api.impl.ldap;

import org.carlspring.strongbox.users.security.AuthoritiesProvider;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextSource;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Przemyslaw Fusik
 */
public class DefaultLdapAuthoritiesPopulator
        extends org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator
{

    private static final Logger logger = LoggerFactory.getLogger(DefaultLdapAuthoritiesPopulator.class);

    @Inject
    private AuthoritiesProvider authoritiesProvider;

    private Map<String, String> ldapRoleToInternalRolesMapping;

    public DefaultLdapAuthoritiesPopulator(ContextSource contextSource,
                                           String groupSearchBase)
    {
        super(contextSource, groupSearchBase);
    }

    @Override
    public Set<GrantedAuthority> getGroupMembershipRoles(String userDn,
                                                         String username)
    {
        if (getGroupSearchBase() == null)
        {
            return new HashSet<>();
        }

        logger.debug("Searching for roles for user '{}', DN = '{}', with filter {} in search base '{}'",
                     username,
                     userDn,
                     getGroupSearchFilter(),
                     getGroupSearchBase());

        Set<String> userRoles = getLdapTemplate().searchForSingleAttributeValues(
                getGroupSearchBase(),
                getGroupSearchFilter(),
                new String[]{ userDn,
                              username },
                getGroupRoleAttribute());

        logger.debug("Roles from search: {}", userRoles);

        Set<GrantedAuthority> authorities = new HashSet<>();
        for (String ldapRole : userRoles)
        {
            if (ldapRoleToInternalRolesMapping.containsKey(ldapRole))
            {
                final String internalRole = ldapRoleToInternalRolesMapping.get(ldapRole);

                logger.debug("Internal role {} found for LDAP role {}", internalRole, ldapRole);

                authorities.addAll(authoritiesProvider.getAuthoritiesByRoleName(internalRole));
            }
        }

        return authorities;
    }

    public void setLdapRoleToInternalRolesMapping(Map<String, String> ldapRoleToInternalRolesMapping)
    {
        this.ldapRoleToInternalRolesMapping = ldapRoleToInternalRolesMapping;
    }
}
