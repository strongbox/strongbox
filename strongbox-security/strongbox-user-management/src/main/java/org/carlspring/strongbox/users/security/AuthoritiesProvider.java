package org.carlspring.strongbox.users.security;

import org.carlspring.strongbox.authorization.AuthorizationConfigFileManager;
import org.carlspring.strongbox.authorization.domain.AuthorizationConfig;
import org.carlspring.strongbox.authorization.domain.Role;
import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
import org.carlspring.strongbox.authorization.service.AuthorizationConfigService;
import org.carlspring.strongbox.users.domain.Roles;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class AuthoritiesProvider
{

    private static final Logger logger = LoggerFactory.getLogger(AuthoritiesProvider.class);

    @Inject
    private AuthorizationConfigService authorizationConfigService;

    @Inject
    private AuthorizationConfigFileManager authorizationConfigFileManager;

    @PostConstruct
    void init()
    {
        final AuthorizationConfigDto config = authorizationConfigFileManager.read();
        authorizationConfigService.setAuthorizationConfig(config);
    }

    public Set<GrantedAuthority> getAuthoritiesByRoleName(final String roleName)
    {
        Set<GrantedAuthority> fullAuthorities = new HashSet<>();
        Set<Role> configuredRoles = new HashSet<>();
        populate(fullAuthorities, configuredRoles);

        Set<GrantedAuthority> authorities = new HashSet<>();

        if (roleName.toUpperCase().equals("ADMIN"))
        {
            authorities.addAll(fullAuthorities);
        }

        // add all privileges from etc/conf/strongbox-authorization.xml for any role that defines there
        configuredRoles.forEach(role ->
                                {
                                    if (role.getName()
                                            .equalsIgnoreCase(roleName))
                                    {
                                        role.getPrivileges()
                                            .forEach(
                                                    privilegeName -> authorities.add(
                                                            new SimpleGrantedAuthority(privilegeName.toUpperCase())));
                                    }
                                });

        try
        {
            Roles configuredRole = Roles.valueOf(roleName);
            authorities.addAll(configuredRole.getPrivileges());
        }
        catch (IllegalArgumentException e)
        {
            logger.warn("Unable to find predefined role by name " + roleName);
        }

        return authorities;
    }

    private void populate(Set<GrantedAuthority> fullAuthorities,
                          Set<Role> configuredRoles)
    {
        AuthorizationConfig authorizationConfig = authorizationConfigService.get();

        authorizationConfig.getRoles()
                           .forEach(role -> role.getPrivileges()
                                                .forEach(
                                                        privilegeName -> fullAuthorities.add(
                                                                new SimpleGrantedAuthority(
                                                                                                  privilegeName.toUpperCase()))));

        configuredRoles.addAll(authorizationConfig.getRoles());
    }
}
