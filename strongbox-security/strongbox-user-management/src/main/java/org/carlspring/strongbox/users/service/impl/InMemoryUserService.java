package org.carlspring.strongbox.users.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

import java.util.stream.Collectors;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.domain.User;
import org.carlspring.strongbox.domain.SecurityRole;
import org.carlspring.strongbox.users.domain.UserData;
import org.carlspring.strongbox.users.domain.Users;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.dto.UsersDto;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.carlspring.strongbox.users.service.UserService;
import org.jose4j.lang.JoseException;
import org.springframework.cache.annotation.CacheEvict;


public class InMemoryUserService implements UserService
{

    protected Map<String, UserDto> userMap = new ConcurrentHashMap<>();

    private final ReadWriteLock usersLock = new ReentrantReadWriteLock();

    @Inject
    private SecurityTokenProvider tokenProvider;

    @Override
    public Users getUsers()
    {
        final Lock readLock = usersLock.readLock();
        readLock.lock();

        try
        {
            Set<UserDto> userSet = new HashSet<>(userMap.values());

            return new Users(new UsersDto(userSet));
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public User findByUsername(final String username)
    {
        if (username == null)
        {
            return null;
        }

        final Lock readLock = usersLock.readLock();
        readLock.lock();

        try
        {
            return Optional.ofNullable(userMap.get(username))
                           .map(UserData::new)
                           .orElse(null);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public String generateSecurityToken(final String username)
            throws JoseException
    {
        final User user = findByUsername(username);

        if (StringUtils.isEmpty(user.getSecurityTokenKey()))
        {
            return null;
        }

        final Map<String, String> claimMap = new HashMap<>();
        claimMap.put(UserData.SECURITY_TOKEN_KEY, user.getSecurityTokenKey());

        return tokenProvider.getToken(username, claimMap, null, null);
    }

    @Override
    public void revokeEveryone(final String roleToRevoke)
    {
        modifyInLock(users -> {
            users.values().forEach(user -> user.removeRole(roleToRevoke));
        });
    }

    @Override
    @CacheEvict(cacheNames = CacheName.User.AUTHENTICATIONS, key = "#p0.username")
    public User save(final User user)
    {
        return modifyInLock(users -> {
            UserDto userDto = Optional.ofNullable(users.get(user.getUsername())).orElseGet(() -> new UserDto());

            if (!StringUtils.isBlank(user.getPassword()))
            {
                userDto.setPassword(user.getPassword());
            }
            userDto.setUsername(user.getUsername());
            userDto.setEnabled(user.isEnabled());
            userDto.setRoles(user.getRoles()
                                 .stream()
                                 .map(SecurityRole::getRoleName)
                                 .collect(Collectors.toSet()));
            userDto.setSecurityTokenKey(user.getSecurityTokenKey());
            userDto.setLastUpdate(LocalDateTime.now());

            users.putIfAbsent(user.getUsername(), userDto);

            return userDto;
        });
    }

    @Override
    public void deleteByUsername(final String username)
    {
        modifyInLock(users -> {
            users.remove(username);
        });
    }

    @Override
    public void updateAccountDetailsByUsername(User userToUpdate)
    {
        modifyInLock(users -> {
            Optional.ofNullable(users.get(userToUpdate.getUsername()))
                    .ifPresent(user -> {
                        if (!StringUtils.isBlank(userToUpdate.getPassword()))
                        {
                            user.setPassword(userToUpdate.getPassword());
                        }

                        updateSecurityToken(user, userToUpdate.getSecurityTokenKey());
                    });
        });
    }

    private void updateSecurityToken(final UserDto user,
                                     final String securityToken)
    {
        if (StringUtils.isNotBlank(securityToken))
        {
            user.setSecurityTokenKey(securityToken);
        }
    }

    protected void modifyInLock(final Consumer<Map<String, UserDto>> operation)
    {
        final Lock writeLock = usersLock.writeLock();
        writeLock.lock();

        try
        {
            operation.accept(userMap);
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    protected <T> T modifyInLock(final Function<Map<String, UserDto>, T> operation)
    {
        final Lock writeLock = usersLock.writeLock();
        writeLock.lock();

        try
        {
            return operation.apply(userMap);
        }
        finally
        {
            writeLock.unlock();
        }
    }

}
