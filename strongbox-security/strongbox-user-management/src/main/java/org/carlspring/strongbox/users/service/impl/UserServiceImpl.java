package org.carlspring.strongbox.users.service.impl;

import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.users.UsersFileManager;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.domain.Users;
import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.dto.UsersDto;
import org.carlspring.strongbox.users.security.AuthoritiesProvider;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.carlspring.strongbox.users.service.UserService;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jose4j.lang.JoseException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author Alex Oreshkevich
 * @author Przemyslaw Fusik
 */
@Service
public class UserServiceImpl
        implements UserService
{

    private final ReadWriteLock usersLock = new ReentrantReadWriteLock();

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private SecurityTokenProvider tokenProvider;

    @Inject
    private UsersFileManager usersFileManager;

    @Inject
    private AuthoritiesProvider authoritiesProvider;

    /**
     * Yes, this is a state object.
     * It is protected by the {@link #usersLock} here
     * and should not be exposed to the world.
     */
    private UsersDto users;

    @Override
    public Users findAll()
    {
        final Lock readLock = usersLock.readLock();
        readLock.lock();

        try
        {
            return new Users(users);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    @Cacheable(cacheNames = CacheName.User.USERS, key = "#p0")
    public User findByUserName(final String username)
    {
        final Lock readLock = usersLock.readLock();
        readLock.lock();

        try
        {
            Optional<UserDto> optionalUserDto = users.findByUserName(username);

            if (optionalUserDto.isPresent())
            {
                UserDto userDto = optionalUserDto.get();

                Set<String> authorities = userDto.getRoles()
                                                 .stream()
                                                 .map(this::getGrantedAuthorities)
                                                 .map(this::getAuthoritiesAsString)
                                                 .flatMap(Collection::stream)
                                                 .collect(Collectors.toCollection(HashSet::new));

                if (authorities.size() > 0)
                {
                    userDto.setAuthorities(authorities);
                    optionalUserDto = Optional.of(userDto);
                }
            }

            return optionalUserDto.map(User::new).orElse(null);
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
        final User user = findByUserName(username);

        if (StringUtils.isEmpty(user.getSecurityTokenKey()))
        {
            return null;
        }

        final Map<String, String> claimMap = new HashMap<>();
        claimMap.put("security-token-key", user.getSecurityTokenKey());

        return tokenProvider.getToken(username, claimMap, null);
    }

    @Override
    public String generateAuthenticationToken(final String username,
                                              final Integer expireMinutes)
            throws JoseException
    {
        final User user = findByUserName(username);

        final Map<String, String> claimMap = new HashMap<>();
        claimMap.put("credentials", user.getPassword());

        return tokenProvider.getToken(username, claimMap, expireMinutes);
    }

    @Override
    public void verifySecurityToken(final String username,
                                    final String apiKey)
    {
        final User user = findByUserName(username);

        final Map<String, String> claimMap = new HashMap<>();
        claimMap.put("security-token-key", user.getSecurityTokenKey());

        tokenProvider.verifyToken(apiKey, username, claimMap);
    }

    @Override
    public void revokeEveryone(final String roleToRevoke)
    {
        modifyInLock(users ->
                     {
                         users.getUsers().forEach(user -> user.removeRole(roleToRevoke));
                     });
    }

    @Override
    @CacheEvict(cacheNames = CacheName.User.USERS, key = "#p0.username")
    public void add(final UserDto user)
    {
        modifyInLock(users ->
                     {
                         final Set<UserDto> currentUsers = users.getUsers();
                         if (currentUsers.stream()
                                         .noneMatch(
                                                 u -> u.getUsername().equals(user.getUsername())
                                         ))
                         {
                             updatePassword(user, user.getPassword());
                             currentUsers.add(user);
                         }
                     });
    }

    @Override
    @CacheEvict(cacheNames = CacheName.User.USERS, key = "#p0")
    public void delete(final String username)
    {
        modifyInLock(users ->
                     {
                         final Set<UserDto> currentUsers = users.getUsers();
                         currentUsers.stream()
                                     .filter(u -> u.getUsername().equals(username))
                                     .findFirst()
                                     .ifPresent(currentUsers::remove);
                     });
    }

    @Override
    @CacheEvict(cacheNames = CacheName.User.USERS, key = "#p0")
    public void updateAccessModel(final String username,
                                  final UserAccessModelDto accessModel)
    {
        modifyInLock(users ->
                     {
                         final Set<UserDto> currentUsers = users.getUsers();
                         currentUsers.stream()
                                     .filter(u -> u.getUsername().equals(username))
                                     .findFirst()
                                     .ifPresent(u -> u.setUserAccessModel(accessModel));
                     });
    }

    @Override
    @CacheEvict(cacheNames = CacheName.User.USERS, key = "#p0.username")
    public void updatePassword(final UserDto userToUpdate)
    {
        modifyInLock(users ->
                     {
                         users.getUsers()
                              .stream()
                              .filter(user -> user.getUsername().equals(userToUpdate.getUsername()))
                              .findFirst()
                              .ifPresent(user -> updatePassword(user, userToUpdate.getPassword()));
                     });
    }

    @Override
    @CacheEvict(cacheNames = CacheName.User.USERS, key = "#p0.username")
    public void updateSecurityToken(final UserDto userToUpdate)
    {
        modifyInLock(users ->
                     {
                         users.getUsers()
                              .stream()
                              .filter(user -> user.getUsername().equals(userToUpdate.getUsername()))
                              .findFirst()
                              .ifPresent(user -> updateSecurityToken(user, userToUpdate.getSecurityTokenKey()));
                     });
    }

    @Override
    @CacheEvict(cacheNames = CacheName.User.USERS, key = "#p0.username")
    public void updateByUsername(final UserDto userToUpdate)
    {
        modifyInLock(users ->
                     {
                         users.getUsers().stream().filter(
                                 user -> user.getUsername().equals(userToUpdate.getUsername())).findFirst().ifPresent(
                                 user ->
                                 {
                                     updatePassword(user, userToUpdate.getPassword());
                                     user.setEnabled(userToUpdate.isEnabled());
                                     user.setRoles(userToUpdate.getRoles());
                                     user.setSecurityTokenKey(userToUpdate.getSecurityTokenKey());
                                     user.setUserAccessModel(userToUpdate.getUserAccessModel());
                                 }
                         );
                     });
    }

    @Override
    @CacheEvict(cacheNames = CacheName.User.USERS, key = "#p0.username")
    public void updateAccountDetailsByUsername(UserDto userToUpdate)
    {
        modifyInLock(users ->
                     {
                         users.getUsers()
                              .stream()
                              .filter(user -> user.getUsername().equals(userToUpdate.getUsername()))
                              .findFirst()
                              .ifPresent(user ->
                                         {
                                             updatePassword(user, userToUpdate.getPassword());
                                             updateSecurityToken(user, userToUpdate.getSecurityTokenKey());
                                         });
                     });
    }

    @Override
    @CacheEvict(cacheNames = CacheName.User.USERS, allEntries = true)
    public void setUsers(final UsersDto newUsers)
    {
        modifyInLock(users ->
                     {
                         UserServiceImpl.this.users = newUsers;
                     },
                     false);
    }


    private void updatePassword(final UserDto user,
                                final String rawPassword)
    {
        if (StringUtils.isNotBlank(rawPassword))
        {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
    }

    private void updateSecurityToken(final UserDto user,
                                     final String securityToken)
    {
        if (StringUtils.isNotBlank(securityToken))
        {
            user.setSecurityTokenKey(securityToken);
        }
    }

    private void modifyInLock(final Consumer<UsersDto> operation)
    {
        modifyInLock(operation, true);
    }

    private void modifyInLock(final Consumer<UsersDto> operation,
                              final boolean storeInFile)
    {
        final Lock writeLock = usersLock.writeLock();
        writeLock.lock();

        try
        {
            operation.accept(users);

            if (storeInFile)
            {
                usersFileManager.store(users);
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }

    private Set<GrantedAuthority> getGrantedAuthorities(String role)
    {
        return authoritiesProvider.getAuthoritiesByRoleName(role);
    }

    private Set<String> getAuthoritiesAsString(Set<GrantedAuthority> authorities)
    {
        return authorities.stream()
                          .map(GrantedAuthority::getAuthority)
                          .collect(Collectors.toCollection(HashSet::new));
    }

}
