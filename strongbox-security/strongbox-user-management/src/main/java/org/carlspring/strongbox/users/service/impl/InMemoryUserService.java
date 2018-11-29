package org.carlspring.strongbox.users.service.impl;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.util.Collection;
import java.util.Collections;
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
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Qualifier;

import org.apache.commons.lang.StringUtils;
import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.domain.Users;
import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserAccessModelReadContract;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.dto.UserReadContract;
import org.carlspring.strongbox.users.dto.UsersDto;
import org.carlspring.strongbox.users.security.AuthoritiesProvider;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.carlspring.strongbox.users.service.UserService;
import org.jose4j.lang.JoseException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@InMemoryUserService.InMemoryUserServiceQualifier
public class InMemoryUserService implements UserService
{

    protected Map<String, UserDto> userMap = new ConcurrentHashMap<>();

    private final ReadWriteLock usersLock = new ReentrantReadWriteLock();

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private SecurityTokenProvider tokenProvider;

    @Inject
    private AuthoritiesProvider authoritiesProvider;

    @Override
    public Users findAll()
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
    public User findByUserName(final String username)
    {
        if (username == null)
        {
            return null;
        }
        
        final Lock readLock = usersLock.readLock();
        readLock.lock();

        try
        {
            Optional<UserDto> optionalUserDto = Optional.ofNullable(userMap.get(username));

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
        claimMap.put(User.SECURITY_TOKEN_KEY, user.getSecurityTokenKey());

        return tokenProvider.getToken(username, claimMap, null);
    }

    @Override
    public String generateAuthenticationToken(final String username,
                                              final Integer expireMinutes)
            throws JoseException
    {
        return tokenProvider.getToken(username, Collections.emptyMap(), expireMinutes);
    }

    @Override
    public void revokeEveryone(final String roleToRevoke)
    {
        modifyInLock(users -> {
            users.values().forEach(user -> user.removeRole(roleToRevoke));
        });
    }

    @Override
    public void save(final UserReadContract user)
    {
        modifyInLock(users -> {
            UserDto u = Optional.ofNullable(users.get(user.getUsername())).orElseGet(() -> new UserDto());

            updatePassword(u, user.getPassword());
            
            u.setUsername(user.getUsername());
            u.setEnabled(user.isEnabled());
            u.setRoles(user.getRoles());
            u.setSecurityTokenKey(user.getSecurityTokenKey());
            u.setUserAccessModel(user.getUserAccessModel());
            
            users.putIfAbsent(user.getUsername(), u);
        });
    }

    @Override
    public void delete(final String username)
    {
        modifyInLock(users -> {
            users.remove(username);
        });
    }

    @Override
    public void updateAccessModel(final String username,
                                  final UserAccessModelDto accessModel)
    {
        modifyInLock(users -> {
            Optional.ofNullable(users.get(username))
                    .ifPresent(u -> u.setUserAccessModel(accessModel));
        });
    }

    @Override
    public void updatePassword(final UserDto userToUpdate)
    {
        modifyInLock(users -> {
            Optional.ofNullable(users.get(userToUpdate.getUsername()))
                    .ifPresent(user -> updatePassword(user, userToUpdate.getPassword()));
        });
    }

    @Override
    public void updateSecurityToken(final UserDto userToUpdate)
    {
        modifyInLock(users -> {
            Optional.ofNullable(users.get(userToUpdate.getUsername()))
                    .ifPresent(user -> updateSecurityToken(user, userToUpdate.getSecurityTokenKey()));
        });
    }

    @Override
    public void updateAccountDetailsByUsername(UserDto userToUpdate)
    {
        modifyInLock(users -> {
            Optional.ofNullable(users.get(userToUpdate.getUsername()))
                    .ifPresent(user -> {
                        updatePassword(user, userToUpdate.getPassword());
                        updateSecurityToken(user, userToUpdate.getSecurityTokenKey());
                    });
        });
    }

    private void updatePassword(final UserDto user,
                                final String rawPassword)
    {
        if (StringUtils.isNotBlank(rawPassword) && !rawPassword.equals(user.getPassword()))
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

    @Documented
    @Retention(RUNTIME)
    @Qualifier
    public @interface InMemoryUserServiceQualifier
    {

    }

}
