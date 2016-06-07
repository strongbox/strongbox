package org.carlspring.strongbox.security.user;

import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;

import java.util.Optional;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Component
public class StrongboxUserDetailService
        implements UserDetailsService
{

    private static final Logger logger = LoggerFactory.getLogger(StrongboxUserDetailService.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    OObjectDatabaseTx databaseTx;

    @Override
    @Transactional
    public synchronized UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException
    {

        logger.info("Loading user by user name: {}", username);
        Optional<User> optionalUser = userService.findByUserName(username);
        if (!optionalUser.isPresent()){
            logger.error("[authenticate] ERROR Cannot find user with that username " + username);
            throw new UsernameNotFoundException("Cannot find user with that username");
        }

        User user = databaseTx.detach(optionalUser.get());

        logger.info("user roles: {}", user.getRoles());

        // extract (detach) user in current transaction
        try
        {
            SpringSecurityUser springUser = new SpringSecurityUser(user);
            return springUser;
        }
        catch (Exception e){
            logger.error("[authenticate] ERROR Unable detach user from db", e);
            return null;
        }
    }
}
