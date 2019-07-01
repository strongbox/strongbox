package org.carlspring.strongbox.users.service.impl;

import org.carlspring.strongbox.users.UsersFileManager;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.dto.UserData;
import org.carlspring.strongbox.users.dto.UsersDto;
import org.carlspring.strongbox.users.service.impl.StrongboxUserService.StrongboxUserServiceQualifier;

import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.IOException;

/**
 * @author Alex Oreshkevich
 * @author Przemyslaw Fusik
 */
@Service
@StrongboxUserServiceQualifier
public class StrongboxUserService
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
    public void save(UserData user)
    {
        encryptPassword((UserDto) user, user.getPassword());
        
        super.save(user);
    }

    @Override
    public void updatePassword(UserDto userToUpdate)
    {
        encryptPassword(userToUpdate, userToUpdate.getPassword());
        
        super.updatePassword(userToUpdate);
    }

    @Override
    public void updateAccountDetailsByUsername(UserDto userToUpdate)
    {
        encryptPassword(userToUpdate, userToUpdate.getPassword());
        
        super.updateAccountDetailsByUsername(userToUpdate);
    }

    @Override
    protected void modifyInLock(Consumer<Map<String, UserDto>> operation)
    {
        super.modifyInLock(operation.andThen(u -> doStoreUsers()));
    }

    protected void doStoreUsers()
    {
        try
        {
            usersFileManager.store(new UsersDto(new HashSet<>(userMap.values())));
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }
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
    public @interface StrongboxUserServiceQualifier
    {
        String value() default "strongboxUserServiceQualifier";
    }
}
