package org.carlspring.strongbox.config;

import org.carlspring.strongbox.users.UsersFileManager;
import org.carlspring.strongbox.users.UsersMapper;
import org.carlspring.strongbox.users.domain.MutableUsers;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.dto.UsersDto;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Spring configuration for all user-related code.
 *
 * @author Alex Oreshkevich
 * @author Przemyslaw Fusik
 */
@Configuration
@ComponentScan({ "org.carlspring.strongbox.users" })
@Import({ DataServiceConfig.class,
          CommonConfig.class,
          UsersAuthorizationConfig.class})
public class UsersConfig
{

    @Inject
    private UserService userService;

    @Inject
    private UsersFileManager usersFileManager;

    @PostConstruct
    public void init()
    {
        final UsersDto securityUsers = usersFileManager.read();
        MutableUsers users = UsersMapper.securityToManagement(securityUsers);
        userService.setUsers(users);
    }


}
