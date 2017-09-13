package org.carlspring.strongbox.users.userdetails;

import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.security.AuthoritiesProvider;
import org.carlspring.strongbox.users.service.UserService;

import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@StrongboxUserDetailService.StrongboxUserDetailServiceQualifier
@Component
public class StrongboxUserDetailService
        implements UserDetailsService
{

    private static final Logger logger = LoggerFactory.getLogger(StrongboxUserDetailService.class);

    // @Autowired
    // private PasswordEncoder passwordEncoder;

    @Inject
    UserService userService;

    @Inject
    AuthoritiesProvider authoritiesProvider;

    @Override
    @Cacheable(value = "userDetails",
            key = "#name")
    public synchronized UserDetails loadUserByUsername(String name)
            throws UsernameNotFoundException
    {
        logger.debug("Loading user details for " + name + " ...");

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
        user.getRoles()
            .forEach(role -> authorities.addAll(authoritiesProvider.getAuthoritiesByRoleName(role.toUpperCase())));

        // extract (detach) user in current transaction
        SpringSecurityUser springUser = new SpringSecurityUser();
        springUser.setEnabled(user.isEnabled());
        springUser.setPassword(user.getPassword());
        springUser.setSalt(user.getSalt());
        springUser.setUsername(user.getUsername());
        springUser.setAuthorities(authorities);
        springUser.setAccessModel(user.getAccessModel());
        springUser.setSecurityKey(user.getSecurityTokenKey());
        logger.info("Authorise under " + springUser);

        return springUser;
    }

    @Documented
    @Retention(RUNTIME)
    @Qualifier
    public @interface StrongboxUserDetailServiceQualifier
    {

    }


}
