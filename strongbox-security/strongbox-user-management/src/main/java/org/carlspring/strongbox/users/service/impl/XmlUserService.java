package org.carlspring.strongbox.users.service.impl;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Qualifier;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.strongbox.users.UsersFileManager;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.dto.UserReadContract;
import org.carlspring.strongbox.users.dto.UsersDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author Alex Oreshkevich
 * @author Przemyslaw Fusik
 */
@Service
@XmlUserService.XmlUserServiceQualifier
public class XmlUserService
        extends InMemoryUserService
{

    @Inject
    private UsersFileManager usersFileManager;

    @Inject
    private PasswordEncoder passwordEncoder;

    private void encryptPassword(final UserDto user,
                                 final String rawPassword)
    {
        if (StringUtils.isNotBlank(rawPassword))
        {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
    }

    @Override
    public void save(UserReadContract user)
    {
        encryptPassword((UserDto) user, user.getPassword());
        
        super.save(user);
    }

    @Override
    public void updatePassword(UserDto userToUpdate)
    {
        encryptPassword((UserDto) userToUpdate, userToUpdate.getPassword());
        
        super.updatePassword(userToUpdate);
    }

    @Override
    public void updateAccountDetailsByUsername(UserDto userToUpdate)
    {
        encryptPassword((UserDto) userToUpdate, userToUpdate.getPassword());
        
        super.updateAccountDetailsByUsername(userToUpdate);
    }

    @Override
    protected void modifyInLock(Consumer<Map<String, UserDto>> operation)
    {
        super.modifyInLock(operation.andThen(u -> usersFileManager.store(new UsersDto(
                new HashSet<>(userMap.values())))));
    }

    public void setUsers(final UsersDto newUsers)
    {
        modifyInLock(users -> {
            users.clear();
            newUsers.getUsers().stream().forEach(u -> users.put(u.getUsername(), u));
        });
    }

    @Documented
    @Retention(RUNTIME)
    @Qualifier
    public @interface XmlUserServiceQualifier
    {

    }
}
