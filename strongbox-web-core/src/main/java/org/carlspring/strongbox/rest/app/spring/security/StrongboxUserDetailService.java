package org.carlspring.strongbox.rest.app.spring.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("strongboxUserDetailService")
public class StrongboxUserDetailService implements UserDetailsService
{
    private static final Logger logger = LoggerFactory.getLogger(StrongboxUserDetailService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        logger.info("Loading user by user name: {}", username);
        StrongboxUser user = userRepository.findByUserName(username);

        if (user == null)
        {
            throw new UsernameNotFoundException("Cannot find user with that username");
        }

        logger.info("user roles: {}", user.getRoles());

        return new SpringSecurityUser(user);
    }

}
