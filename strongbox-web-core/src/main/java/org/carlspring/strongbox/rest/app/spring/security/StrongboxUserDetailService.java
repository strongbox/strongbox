package org.carlspring.strongbox.rest.app.spring.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StrongboxUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        StrongboxUser user = userRepository.findByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("Cannot find user with that username");
        }

        return new SpringSecurityUser(user);
    }
}
