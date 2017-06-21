package org.carlspring.strongbox.authentication.api.impl.ldap;

import org.carlspring.strongbox.authentication.support.AuthoritiesExternalToInternalMapper;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.ContextSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

/**
 * @author Przemyslaw Fusik
 */
public class DefaultLdapAuthoritiesPopulator
        extends org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator
        implements
        InitializingBean
{

    private static final Logger logger = LoggerFactory.getLogger(DefaultLdapAuthoritiesPopulator.class);

    private AuthoritiesExternalToInternalMapper authoritiesExternalToInternalMapper;

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

        return new HashSet<>(authoritiesExternalToInternalMapper.mapRoles(userRoles));
    }

    public void setAuthoritiesExternalToInternalMapper(AuthoritiesExternalToInternalMapper authoritiesExternalToInternalMapper)
    {
        this.authoritiesExternalToInternalMapper = authoritiesExternalToInternalMapper;
    }

    @Override
    public void afterPropertiesSet()
            throws Exception
    {
        Assert.notNull(authoritiesExternalToInternalMapper, "authoritiesExternalToInternalMapper property not set");
    }
}
