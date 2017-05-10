package org.carlspring.strongbox.authentication.registry;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.registry.support.xml.out.AuthenticatorsAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.ImmutableList;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class AuthenticatorsRegistry
{

    final transient ReentrantLock lock = new ReentrantLock();

    private final List<Authenticator> authenticators;

    private List<Authenticator> lastSnapshot;

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
    @XmlElement(name = "authenticators")
    @XmlJavaTypeAdapter(AuthenticatorsAdapter.class)
    public List<Authenticator> getAuthenticators()
    {
        // Don't lock this method as it is going to be heavily used
        return lock.isLocked() ? lastSnapshot : updateSnapshot();
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        final List<Authenticator> view = getAuthenticators();
        for (int index = 0; index < view.size(); index++)
        {
            final Authenticator authenticator = view.get(index);
            builder.append(Arrays.toString(new Object[]{ index,
                                                         authenticator.getName() }));
        }
        return builder.toString();
    }
}
