package org.carlspring.strongbox.rest.app.spring.security;

import org.carlspring.strongbox.data.domain.User;
import org.carlspring.strongbox.data.service.UserService;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class StrongboxUserDetailService
        implements UserDetailsService
{

    private static final Logger logger = LoggerFactory.getLogger(StrongboxUserDetailService.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @PostConstruct
    void setup()
    {

        final String admin = "admin";

        // add a user for testing
        // Usually OrientDB should be setup separately(i.e.remotely) and
        // users should be added either via REST endpoint or via OrientDB Studio/Console
        if (!userService.findByUserName(admin).isPresent())
        {
            User user = new User();
            user.setUsername(admin);
            user.setPassword(passwordEncoder.encode("password"));
            user.setRoles(Collections.singletonList("ROLE_ADMIN"));
            user.setEnabled(true);
            userService.save(user);
        }
    }

    @Override
    @Transactional
    public synchronized UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException
    {

        logger.info("Loading user by user name: {}", username);
        Optional<User> optionalUser = userService.findByUserName(username);
        optionalUser.orElseThrow(() -> new UsernameNotFoundException("Cannot find user with that username"));

        User user = optionalUser.get();

        logger.info("user roles: {}", user.getRoles());

        // extract (detach) user in current transaction
        SpringSecurityUser springUser = new SpringSecurityUser();
        springUser.setEnabled(user.isEnabled());
        springUser.setPassword(user.getPassword());
        springUser.setRoles(user.getRoles());
        springUser.setSalt(user.getSalt());
        springUser.setUsername(user.getUsername());

        return springUser;
    }
}
