package org.carlspring.strongbox.authorization.service.impl;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.carlspring.strongbox.authorization.AuthorizationConfigFileManager;
import org.carlspring.strongbox.authorization.domain.AuthorizationConfig;
import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
import org.carlspring.strongbox.authorization.dto.RoleDto;
import org.carlspring.strongbox.authorization.service.AuthorizationConfigService;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.domain.SystemRole;
import org.springframework.stereotype.Service;


/**
 * @author Alex Oreshkevich
 * @author Przemyslaw Fusik
 */
@Service
public class AuthorizationConfigServiceImpl
        implements AuthorizationConfigService
{

    private final ReadWriteLock authorizationConfigLock = new ReentrantReadWriteLock();

    @Inject
    private AuthorizationConfigFileManager authorizationConfigFileManager;

    /**
     * Yes, this is a state object.
     * It is protected by the {@link #authorizationConfigLock} here
     * and should not be exposed to the world.
     */
    private AuthorizationConfigDto authorizationConfig;

    @Override
    public void setAuthorizationConfig(final AuthorizationConfigDto newConfig) throws IOException
    {
        modifyInLock(config ->
                     {
                         AuthorizationConfigServiceImpl.this.authorizationConfig = newConfig;
                     },
                     true);
    }

    @Override
    public AuthorizationConfigDto getDto()
    {
        final Lock readLock = authorizationConfigLock.readLock();
        readLock.lock();

        try
        {
            return SerializationUtils.clone(authorizationConfig);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public AuthorizationConfig get()
    {
        final Lock readLock = authorizationConfigLock.readLock();
        readLock.lock();

        try
        {
            return new AuthorizationConfig(authorizationConfig);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void addRole(final RoleDto role) throws IOException
    {
        modifyInLock(config ->
                     {
                         config.getRoles().add(role);
                     });
    }

    @Override
    public boolean deleteRole(final String roleName) throws IOException
    {
        MutableBoolean result = new MutableBoolean();
        modifyInLock(config ->
                     {
                         Set<RoleDto> roles = config.getRoles();
                         roles.stream()
                              .filter(r -> r.getName()
                                            .equalsIgnoreCase(roleName))
                              .findFirst()
                              .ifPresent(r -> result.setValue(roles.remove(r)));
                     });
        return result.isTrue();
    }

    @Override
    public void addPrivilegesToAnonymous(final List<Privileges> privilegeList) throws IOException
    {
        modifyInLock(config ->
                     {
                         Set<RoleDto> roles = config.getRoles();
                         roles.stream()
                              .filter(r -> r.getName()
                                            .equalsIgnoreCase(SystemRole.ANONYMOUS.name()))
                              .findFirst()
                              .ifPresent(r -> privilegeList.stream()
                                                           .forEach(p -> r.addPrivilege(p)));
                     });
    }

    private void modifyInLock(final Consumer<AuthorizationConfigDto> operation) throws IOException
    {
        modifyInLock(operation, true);
    }

    private void modifyInLock(final Consumer<AuthorizationConfigDto> operation,
                              final boolean storeInFile) throws IOException
    {
        final Lock writeLock = authorizationConfigLock.writeLock();
        writeLock.lock();

        try
        {
            operation.accept(authorizationConfig);

            if (storeInFile)
            {
                authorizationConfigFileManager.store(authorizationConfig);
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }

}
