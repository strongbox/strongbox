package org.carlspring.strongbox.authorization.service.impl;

import org.carlspring.strongbox.authorization.AuthorizationConfigFileManager;
import org.carlspring.strongbox.authorization.domain.AuthorizationConfig;
import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
import org.carlspring.strongbox.authorization.dto.PrivilegeDto;
import org.carlspring.strongbox.authorization.dto.RoleDto;
import org.carlspring.strongbox.authorization.service.AuthorizationConfigService;
import org.carlspring.strongbox.configuration.ConfigurationException;
import org.carlspring.strongbox.users.domain.Roles;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
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

    private static void throwIfNotEmpty(Sets.SetView<String> intersectionView,
                                        String message)
    {
        if (!intersectionView.isEmpty())
        {
            throw new ConfigurationException(message + intersectionView);
        }
    }

    private static Set<String> collect(@NotNull Iterable<?> it,
                                       @NotNull NameFunction nameFunction)
    {
        Set<String> names = new HashSet<>();
        for (Object o : it)
        {
            names.add(nameFunction.name(o));
        }
        return names;
    }

    @Override
    public void setAuthorizationConfig(final AuthorizationConfigDto newConfig)
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
        catch (Exception e)
        {
            throw Throwables.propagate(e);
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
    public void addRole(final RoleDto role)
    {
        modifyInLock(config ->
                     {
                         AuthorizationConfigDto configClone = SerializationUtils.clone(config);
                         configClone.getRoles().add(role);
                         validateConfig(configClone);

                         config.getRoles().add(role);
                     });
    }

    @Override
    public boolean deleteRole(final String roleName)
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
    public void addPrivilegesToAnonymous(final List<PrivilegeDto> privilegeList)
    {
        modifyInLock(config ->
                     {
                         Set<RoleDto> roles = config.getRoles();
                         roles.stream()
                              .filter(r -> r.getName()
                                            .equalsIgnoreCase(ANONYMOUS_ROLE))
                              .findFirst()
                              .ifPresent(r -> privilegeList.stream()
                                                           .map(PrivilegeDto::getName)
                                                           .forEach(p -> r.addPrivilege(p)));
                     });
    }

    private void modifyInLock(final Consumer<AuthorizationConfigDto> operation)
    {
        modifyInLock(operation, true);
    }

    private void modifyInLock(final Consumer<AuthorizationConfigDto> operation,
                              final boolean storeInFile)
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

    private void validateConfig(@NotNull AuthorizationConfigDto config)
            throws ConfigurationException
    {
        // check that embedded roles was not overridden
        throwIfNotEmpty(toIntersection(config.getRoles(),
                                       Arrays.asList(Roles.values()),
                                       o -> ((RoleDto) o).getName()
                                                         .toUpperCase(),
                                       o -> ((Roles) o).name()
                                                       .toUpperCase()),
                        "Embedded roles overriding is forbidden: ");
    }

    /**
     * Calculates intersection of two sets that was created from two iterable sources with help of two name functions
     * respectively.
     */
    private Sets.SetView<String> toIntersection(@NotNull Iterable<?> first,
                                                @NotNull Iterable<?> second,
                                                @NotNull NameFunction firstNameFunction,
                                                @NotNull NameFunction secondNameFunction)
    {
        return Sets.intersection(collect(first, firstNameFunction), collect(second, secondNameFunction));
    }

    // used to receive String representation of any object to execute future comparisons based on that
    private interface NameFunction
    {

        String name(Object o);
    }
}
