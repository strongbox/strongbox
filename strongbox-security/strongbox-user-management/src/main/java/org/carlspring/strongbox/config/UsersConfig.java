package org.carlspring.strongbox.config;

import org.carlspring.strongbox.users.UsersFileManager;
import org.carlspring.strongbox.users.dto.UsersDto;
import org.carlspring.strongbox.users.service.UserService;

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
          UsersAuthorizationConfig.class })
public class UsersConfig
{

    @Inject
    private UserService userService;

    @Inject
    private UsersFileManager usersFileManager;

    @PostConstruct
    void init()
    {
        final UsersDto securityUsers = usersFileManager.read();
        userService.setUsers(securityUsers);
    }


}
