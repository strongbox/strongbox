package org.carlspring.strongbox.authentication.support;

import org.carlspring.strongbox.users.security.AuthoritiesProvider;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.util.Assert;

/**
 * @author Przemyslaw Fusik
 */
@ThreadSafe
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class AuthoritiesExternalToInternalMapper
        implements InitializingBean, GrantedAuthoritiesMapper
{

    private static final Logger logger = LoggerFactory.getLogger(AuthoritiesExternalToInternalMapper.class);

    @Inject
    private AuthoritiesProvider authoritiesProvider;

    @XmlElement(name = "rolesMapping")
    private ConcurrentMap<String, String> rolesMapping;

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> externalAuthorities)
    {
        return mapRoles(externalAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
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
            throws Exception
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

    public String upsertRoleMapping(String externalRole,
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
