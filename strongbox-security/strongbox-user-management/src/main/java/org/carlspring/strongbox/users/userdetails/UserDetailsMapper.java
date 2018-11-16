package org.carlspring.strongbox.users.userdetails;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.security.AuthoritiesProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsMapper implements StrongboxUserToUserDetails
{

    @Inject
    private AuthoritiesProvider authoritiesProvider;

    @Override
    public SpringSecurityUser apply(User user)
    {

        Set<GrantedAuthority> authorities = new HashSet<>();
        user.getRoles()
            .forEach(role -> authorities.addAll(authoritiesProvider.getAuthoritiesByRoleName(role.toUpperCase())));

        SpringSecurityUser springUser = new SpringSecurityUser();
        springUser.setEnabled(user.isEnabled());
        springUser.setPassword(user.getPassword());
        springUser.setUsername(user.getUsername());
        springUser.setAuthorities(authorities);
        springUser.setAccessModel(user.getAccessModel());
        springUser.setSecurityKey(user.getSecurityTokenKey());

        return springUser;
    }

}
