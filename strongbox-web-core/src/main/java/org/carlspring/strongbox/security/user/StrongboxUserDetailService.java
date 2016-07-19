package org.carlspring.strongbox.security.user;

import org.carlspring.strongbox.security.jaas.Role;
import org.carlspring.strongbox.users.domain.Roles;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;
import org.carlspring.strongbox.users.service.UserService;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class StrongboxUserDetailService
        implements UserDetailsService
{

    private static final Logger logger = LoggerFactory.getLogger(StrongboxUserDetailService.class);

    // @Autowired
    // private PasswordEncoder passwordEncoder;

    @Autowired
    UserService userService;

    @Autowired
    AuthorizationConfigProvider authorizationConfigProvider;

    @Autowired
    private OObjectDatabaseTx databaseTx;

    private Set<GrantedAuthority> fullAuthorities;

    private Set<Role> configuredRoles;

    @PostConstruct
    public void init()
    {
        fullAuthorities = new HashSet<>();
        configuredRoles = new HashSet<>();

        authorizationConfigProvider.getConfig().ifPresent(
                config -> {

                    config.getPrivileges().getPrivileges().forEach(
                            privilege -> fullAuthorities.add(
                                    new SimpleGrantedAuthority(privilege.getName().toUpperCase())));

                    configuredRoles.addAll(config.getRoles().getRoles());
                }
        );
    }

    @Override
    @Cacheable(value = "userDetails", key = "#name")
    public synchronized UserDetails loadUserByUsername(String name)
            throws UsernameNotFoundException
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Username cannot be null.");
        }

        User user = userService.findByUserName(name);
        if (user == null)
        {
            logger.error("[authenticate] ERROR Cannot find user with that name " + name);
            throw new UsernameNotFoundException("Cannot find user with that name");
        }

        // thread-safe transformation of roles to authorities
        Set<GrantedAuthority> authorities = new HashSet<>();
        user.getRoles().forEach(role -> authorities.addAll(getAuthoritiesByRoleName(role.toUpperCase())));

        // extract (detach) user in current transaction
        SpringSecurityUser springUser = new SpringSecurityUser();
        springUser.setEnabled(user.isEnabled());
        springUser.setPassword(user.getPassword());
        springUser.setSalt(user.getSalt());
        springUser.setUsername(user.getUsername());
        springUser.setAuthorities(authorities);

        logger.debug("Authorise under " + springUser);

        return springUser;
    }

    private synchronized Set<GrantedAuthority> getAuthoritiesByRoleName(final String roleName)
    {

        Set<GrantedAuthority> authorities = new HashSet<>();

        if (roleName.equals("ADMIN"))
        {
            authorities.addAll(fullAuthorities);
        }

        // add all privileges from etc/conf/security-authorization.xml for any role that defines there
        configuredRoles.forEach(role -> {

            databaseTx.activateOnCurrentThread();
            final Role detached = databaseTx.detachAll(role, true);
            if (detached.getName().equalsIgnoreCase(roleName))
            {
                detached.getPrivileges().forEach(
                        privilegeName -> authorities.add(new SimpleGrantedAuthority(privilegeName.toUpperCase())));
            }
        });

        try
        {
            Roles configuredRole = Roles.valueOf(roleName);
            authorities.addAll(configuredRole.getPrivileges());
        }
        catch (IllegalArgumentException e)
        {
            logger.warn("Unable to find predefined role by name " + roleName, e);
        }

        return authorities;
    }

}
