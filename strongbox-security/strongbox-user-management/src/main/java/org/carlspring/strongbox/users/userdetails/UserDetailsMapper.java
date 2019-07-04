package org.carlspring.strongbox.users.userdetails;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.carlspring.strongbox.users.domain.UserData;
import org.carlspring.strongbox.users.security.AuthoritiesProvider;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsMapper implements StrongboxUserToUserDetails
{

    @Inject
    private AuthoritiesProvider authoritiesProvider;

    @Override
    public SpringSecurityUser apply(UserData user)
    {

        SpringSecurityUser springUser = new SpringSecurityUser();
        springUser.setEnabled(user.isEnabled());
        springUser.setPassword(user.getPassword());
        springUser.setUsername(user.getUsername());
        springUser.setRoles(user.getRoles().stream().map(r -> authoritiesProvider.getRuntimeRole(r)).collect(Collectors.toSet()));
        springUser.setSecurityKey(user.getSecurityTokenKey());

        return springUser;
    }

}
