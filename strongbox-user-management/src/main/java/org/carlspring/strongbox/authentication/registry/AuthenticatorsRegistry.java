package org.carlspring.strongbox.authentication.registry;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.registry.support.AuthenticatorsRegistrySerializer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableList;

/**
 * @author Przemyslaw Fusik
 */
@JsonSerialize(using = AuthenticatorsRegistrySerializer.class)
public class AuthenticatorsRegistry
        implements Supplier<List<Authenticator>>
{

    final transient ReentrantLock lock = new ReentrantLock();

    private final List<Authenticator> authenticators;

    private List<Authenticator> lastSnapshot;

    @Inject
    private Optional<ObjectMapper> objectMapper;

    public AuthenticatorsRegistry()
    {
        this(Collections.emptyList());
    }


    public AuthenticatorsRegistry(List<Authenticator> authenticators)
    {
        this.authenticators = new ArrayList<>(authenticators);
        updateSnapshot();
    }

    /**
     * Reloads the registry by replacing all authenticators using
     * given collection.
     *
     * @param authenticators new collection of authenticators
     */
    public void reload(Collection<Authenticator> authenticators)
    {
        Objects.requireNonNull(authenticators, () -> "Required non-null replacing authenticators.");
        lock.lock();
        try
        {
            updateSnapshot();
            this.authenticators.clear();
            this.authenticators.addAll(authenticators);
        }
        finally
        {
            updateSnapshot();
            lock.unlock();
        }
    }

    /**
     * Reorders elements in the registry.
     */
    public void reorder(int first,
                        int second)
    {
        if (first == second)
        {
            return;
        }
        lock.lock();
        try
        {
            updateSnapshot();
            Collections.swap(authenticators, first, second);
        }
        finally
        {
            updateSnapshot();
            lock.unlock();
        }
    }

    private List<Authenticator> updateSnapshot()
    {
        lastSnapshot = ImmutableList.copyOf(authenticators);
        return lastSnapshot;
    }

    /**
     * Returns an immutable copy list containing the authenticators elements, in order.
     * Copy list, once returned, never changes, those it may not be affected by internal list
     * modification methods.
     */
    @Override
    public List<Authenticator> get()
    {
        // Don't lock this method as it is going to be heavily used
        return lock.isLocked() ? lastSnapshot : updateSnapshot();
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        final List<Authenticator> view = get();
        for (int index = 0; index < view.size(); index++)
        {
            final Authenticator authenticator = view.get(index);
            builder.append(Arrays.toString(new Object[]{ index,
                                                         authenticator.getName() }));
        }
        return builder.toString();
    }

    @PostConstruct
    void registerSerializer()
    {
        objectMapper.ifPresent(
                om -> om.registerModule(new SimpleModule().addSerializer(new AuthenticatorsRegistrySerializer())));
    }
}