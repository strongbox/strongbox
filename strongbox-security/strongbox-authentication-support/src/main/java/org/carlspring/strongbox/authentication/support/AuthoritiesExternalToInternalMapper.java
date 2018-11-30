package org.carlspring.strongbox.authentication.support;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.carlspring.strongbox.users.security.AuthoritiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

/**
 * @author Przemyslaw Fusik
 */
public class AuthoritiesExternalToInternalMapper
        implements InitializingBean,
                   GrantedAuthoritiesMapper
{

    private static final Logger logger = LoggerFactory.getLogger(AuthoritiesExternalToInternalMapper.class);

    @Inject
    private AuthoritiesProvider authoritiesProvider;

    private ConcurrentMap<String, String> rolesMapping;

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> externalAuthorities)
    {
        logger.debug(String.format("Map authorities [%s]", externalAuthorities));
        Collection<? extends GrantedAuthority> result = mapRoles(
                externalAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        logger.debug(String.format("Authorities mapped authorities [%s]", result));
        return result;
    }

    private Collection<? extends GrantedAuthority> mapRoles(Collection<String> externalRoles)
    {
        Map<String, String> rolesMappingSnapshot = getRolesMapping();

        Set<GrantedAuthority> authorities = new HashSet<>();
        for (String externalRole : externalRoles)
        {
            final String internalRole = rolesMappingSnapshot.get(externalRole);

            if (internalRole != null)
            {
                logger.debug("Internal role {} found for external role {}", internalRole, externalRole);

                authorities.addAll(authoritiesProvider.getAuthoritiesByRoleName(internalRole));
            }
        }

        return authorities;
    }

    @Override
    public void afterPropertiesSet()
    {
        Assert.notEmpty(rolesMapping, "rolesMapping property not set");
    }

    /**
     * @return Returns an immutable map containing the same entries as {@link #rolesMapping}
     */
    public Map<String, String> getRolesMapping()
    {
        return ImmutableMap.copyOf(rolesMapping);
    }

    public void setRolesMapping(Map<String, String> rolesMapping)
    {
        Assert.notNull(rolesMapping, "rolesMapping cannot be null");
        this.rolesMapping = new ConcurrentHashMap<>(rolesMapping);
    }

    public String putRoleMapping(String externalRole,
                                 String internalRole)
    {
        return this.rolesMapping.put(externalRole, internalRole);
    }

    public String addRoleMapping(String externalRole,
                                 String internalRole)
    {
        return this.rolesMapping.putIfAbsent(externalRole, internalRole);
    }


    public String deleteRoleMapping(String externalRole)
    {
        return this.rolesMapping.remove(externalRole);
    }
}
