package org.carlspring.strongbox.security.user;

import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.domain.Roles;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
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
        Set<Privileges> authorities = new HashSet<>();
        user.getRoles().forEach(role -> {

            // load role by name
            String roleName = role.toUpperCase();
            try
            {
                Roles configuredRole = Roles.valueOf(roleName);
                authorities.addAll(configuredRole.getPrivileges());
            }
            catch (IllegalArgumentException e)
            {
                logger.warn("Unable to find role " + roleName, e);
            }
        });

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
}
