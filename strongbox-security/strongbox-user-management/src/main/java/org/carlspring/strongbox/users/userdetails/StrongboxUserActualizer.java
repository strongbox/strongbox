package org.carlspring.strongbox.users.userdetails;

import javax.inject.Inject;

import org.carlspring.strongbox.users.domain.UserData;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.service.impl.InMemoryUserService.InMemoryUserServiceQualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class StrongboxUserActualizer implements UserDetailsToStrongboxUser
{

    @Inject
    @InMemoryUserServiceQualifier
    private UserService userService;

    @Override
    public UserData apply(UserDetails springUser)
    {
        UserData user = springUser instanceof StrongboxUserDetails ? ((StrongboxUserDetails) springUser).getUser()
                : new UserData(springUser);

        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setPassword(user.getPassword());
        userDto.setEnabled(user.isEnabled());
        userDto.setRoles(user.getRoles());
        userDto.setSecurityTokenKey(user.getSecurityTokenKey());
        userService.save(userDto);

        return userService.findByUserName(springUser.getUsername());
    }

}
