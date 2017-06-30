package org.carlspring.strongbox.authentication.support;

import org.carlspring.strongbox.users.security.AuthoritiesProvider;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

/**
 * @author Przemyslaw Fusik
 */
public class AuthoritiesExternalToInternalMapper
        implements InitializingBean
{

    private static final Logger logger = LoggerFactory.getLogger(AuthoritiesExternalToInternalMapper.class);

    @Inject
    private AuthoritiesProvider authoritiesProvider;

    private Map<String, String> rolesMapping;

    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> externalAuthorities)
    {
        return mapRoles(
                externalAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
    }

    public Collection<? extends GrantedAuthority> mapRoles(Collection<String> externalRoles)
    {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (String externalRole : externalRoles)
        {
            if (rolesMapping.containsKey(externalRole))
            {
                final String internalRole = rolesMapping.get(externalRole);

                logger.debug("Internal role {} found for external role {}", internalRole, externalRole);

                authorities.addAll(authoritiesProvider.getAuthoritiesByRoleName(internalRole));
            }
        }
        return authorities;
    }

    @Override
    public void afterPropertiesSet()
            throws Exception
    {
        Assert.notEmpty(rolesMapping, "rolesMapping property not set");
    }

    public void setRolesMapping(Map<String, String> rolesMapping)
    {
        this.rolesMapping = rolesMapping;
    }
}
