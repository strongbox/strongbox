package org.carlspring.strongbox.users.userdetails;

import javax.inject.Inject;

import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserAccessModelReadContract;
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
    public User apply(UserDetails springUser)
    {
        User user = springUser instanceof StrongboxUserDetails ? ((StrongboxUserDetails) springUser).getUser()
                : new User(springUser);

        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setPassword(user.getPassword());
        userDto.setEnabled(user.isEnabled());
        userDto.setRoles(user.getRoles());
        userDto.setSecurityTokenKey(user.getSecurityTokenKey());
        
        UserAccessModelReadContract accessModel = user.getUserAccessModel();
        if (accessModel != null) {
            UserAccessModelDto acessModelDto = new UserAccessModelDto();
            userDto.setUserAccessModel(acessModelDto);
        }
        userService.save(userDto);

        return userService.findByUserName(springUser.getUsername());
    }

}
