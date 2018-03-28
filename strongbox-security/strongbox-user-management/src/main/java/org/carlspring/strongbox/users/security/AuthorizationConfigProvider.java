package org.carlspring.strongbox.users.security;

import org.carlspring.strongbox.configuration.ConfigurationException;
import org.carlspring.strongbox.data.service.SingletonCrudService;
import org.carlspring.strongbox.data.service.SingletonEntityProvider;
import org.carlspring.strongbox.security.Role;
import org.carlspring.strongbox.users.domain.Roles;
import org.carlspring.strongbox.users.service.AuthorizationConfigService;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;

/**
 * Responsible for load, validate and save to the persistent storage {@link AuthorizationConfig} from configuration
 * sources.
 *
 * @author Alex Oreshkevich
 * @see {@linkplain=https://dev.carlspring.org/youtrack/issue/SB-126}
 */
@Component
public class AuthorizationConfigProvider
        extends SingletonEntityProvider<AuthorizationConfig, String>
{

    @Inject
    private AuthorizationConfigService authorizationConfigService;

    @Inject
    private AuthorizationConfigFileManager authorizationConfigFileManager;

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
    public SingletonCrudService<AuthorizationConfig, String> getService()
    {
        return authorizationConfigService;
    }

    @Override
    protected void preSave(final AuthorizationConfig config)
    {
        validateConfig(config);
    }

    @Override
    protected void postSave(final AuthorizationConfig config)
    {
        authorizationConfigFileManager.store(config);
    }

    @Override
    protected void postGet(final AuthorizationConfig config)
    {
        validateConfig(config);
    }

    private void validateConfig(@NotNull AuthorizationConfig config)
            throws ConfigurationException
    {
        // check that embedded roles was not overridden
        throwIfNotEmpty(toIntersection(config.getRoles(),
                                       Arrays.asList(Roles.values()),
                                       o -> ((Role) o).getName()
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
