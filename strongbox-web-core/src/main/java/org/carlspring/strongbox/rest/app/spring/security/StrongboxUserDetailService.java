package org.carlspring.strongbox.rest.app.spring.security;

import org.carlspring.strongbox.data.domain.StrongboxUser;
import org.carlspring.strongbox.data.service.StrongboxUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;

@Service("strongboxUserDetailService")
public class StrongboxUserDetailService implements UserDetailsService
{
    private static final Logger logger = LoggerFactory.getLogger(StrongboxUserDetailService.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StrongboxUserService userService;

    @PostConstruct
    void setup(){

        final String admin = "admin";

        // add a user for testing
        // Usually OrientDB should be setup separately(i.e.remotely) and
        // users should be added either via REST endpoint or via OrientDB Studio/Console
        if (!userService.findByUserName(admin).isPresent()) {
            StrongboxUser user = new StrongboxUser();
            user.setUsername(admin);
            user.setPassword(passwordEncoder.encode("password"));
            user.setRoles(Collections.singletonList("ROLE_ADMIN"));
            user.setEnabled(true);
            userService.save(user);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        logger.info("Loading user by user name: {}", username);
        StrongboxUser user = userService.findByUserName(username).get();

        if (user == null)
        {
            throw new UsernameNotFoundException("Cannot find user with that username");
        }

        logger.info("user roles: {}", user.getRoles());

        return new SpringSecurityUser(user);
    }

}
