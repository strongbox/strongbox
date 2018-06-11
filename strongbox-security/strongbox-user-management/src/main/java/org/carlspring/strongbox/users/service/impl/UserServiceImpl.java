package org.carlspring.strongbox.users.service.impl;

import org.carlspring.strongbox.users.UsersFileManager;
import org.carlspring.strongbox.users.UsersMapper;
import org.carlspring.strongbox.users.domain.MutableAccessModel;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.domain.Users;
import org.carlspring.strongbox.users.domain.MutableUser;
import org.carlspring.strongbox.users.domain.MutableUsers;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.carlspring.strongbox.users.service.UserService;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.jose4j.lang.JoseException;
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

    /**
     * Yes, this is a state object.
     * It is protected by the {@link #usersLock} here
     * and should not be exposed to the world.
     */
    private MutableUsers users;

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
    public User findByUserName(final String username)
    {
        final Lock readLock = usersLock.readLock();
        readLock.lock();

        try
        {
            final Optional<MutableUser> user = users.findByUserName(username);
            return user.map(User::new).orElse(null);
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
                         users.getUsers().forEach(user -> user.getRoles().remove(roleToRevoke));
                     });
    }

    @Override
    public void add(final MutableUser user)
    {
        modifyInLock(users ->
                     {
                         final Set<MutableUser> currentUsers = users.getUsers();
                         if (!currentUsers.stream().filter(
                                 u -> u.getUsername().equals(user.getUsername())).findFirst().isPresent())
                         {
                             currentUsers.add(user);
                         }
                     });
    }

    @Override
    public void delete(final String username)
    {
        modifyInLock(users ->
                     {
                         final Set<MutableUser> currentUsers = users.getUsers();
                         currentUsers.stream().filter(u -> u.getUsername().equals(username)).findFirst().ifPresent(
                                 u -> currentUsers.remove(u));
                     });
    }

    @Override
    public void updateAccessModel(final String username,
                                  final MutableAccessModel accessModel)
    {
        modifyInLock(users ->
                     {
                         final Set<MutableUser> currentUsers = users.getUsers();
                         currentUsers.stream().filter(u -> u.getUsername().equals(username)).findFirst().ifPresent(
                                 u -> u.setAccessModel(accessModel));
                     });
    }

    @Override
    public void updatePassword(final MutableUser userToUpdate)
    {
        modifyInLock(users ->
                     {
                         users.getUsers().stream().filter(
                                 user -> user.getUsername().equals(userToUpdate.getUsername())).findFirst().ifPresent(
                                 user ->
                                 {
                                     updatePassword(user, userToUpdate.getPassword());
                                 }
                         );
                     });
    }

    @Override
    public void updateByUsername(final MutableUser userToUpdate)
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
                                     user.setAccessModel(userToUpdate.getAccessModel());
                                 }
                         );
                     });
    }

    @Override
    public void setUsers(final MutableUsers newUsers)
    {
        modifyInLock(users ->
                     {
                         UserServiceImpl.this.users = newUsers;
                     },
                     false);
    }


    private void updatePassword(final MutableUser user,
                                final String rawPassword)
    {
        if (StringUtils.isNotEmpty(rawPassword))
        {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
    }

    private void modifyInLock(final Consumer<MutableUsers> operation)
    {
        modifyInLock(operation, true);
    }

    private void modifyInLock(final Consumer<MutableUsers> operation,
                              final boolean storeInFile)
    {
        final Lock writeLock = usersLock.writeLock();
        writeLock.lock();

        try
        {
            operation.accept(users);

            if (storeInFile)
            {
                usersFileManager.store(UsersMapper.managementToSecurity(users));
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }

}
