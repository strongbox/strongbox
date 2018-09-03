package org.carlspring.strongbox.authentication.registry;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.registry.support.xml.AuthenticatorsAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "authenticators-registry")
@XmlAccessorType(XmlAccessType.NONE)
public class AuthenticatorsRegistry
        implements Iterable<Authenticator>
{

    @XmlElement(name = "authenticators")
    @XmlJavaTypeAdapter(AuthenticatorsAdapter.class)
    private volatile Authenticator[] array;

    /**
     * Creates an empty registry.
     */
    public AuthenticatorsRegistry()
    {
        reloadInternally(Collections.emptyList());
    }

    /**
     * Creates a registry containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     */
    public AuthenticatorsRegistry(Collection<? extends Authenticator> c)
    {
        reloadInternally(c);
    }

    private Authenticator[] getArray()
    {
        return array;
    }

    /**
     * Replaces the authenticator of the same class
     * or adds given authenticator to the end of the authenticator lists
     */
    public synchronized void put(Authenticator authenticator)
    {
        List<Authenticator> elements = new ArrayList<>(Arrays.asList(getArray()));
        Optional<Authenticator> opt = elements.stream()
                                              .filter(e -> e.getClass().isAssignableFrom(authenticator.getClass()))
                                              .findFirst();

        if (opt.isPresent())
        {
            int index = elements.indexOf(opt.get());
            elements.set(index, authenticator);
        }
        else
        {
            elements.add(authenticator);
        }

        reloadInternally(elements);
    }

    /**
     * Reloads the registry by replacing all authenticators using
     * given collection.
     *
     * @param c new collection of authenticators
     */
    public synchronized void reload(Collection<? extends Authenticator> c)
    {
        reloadInternally(c);
    }

    /**
     * Reorders elements in the registry.
     */
    public synchronized void reorder(int first,
                                     int second)
    {
        List<Authenticator> elements = new ArrayList<>(Arrays.asList(getArray()));
        final Authenticator firstA = elements.get(first);
        final Authenticator secondA = elements.get(second);
        elements.set(first, secondA);
        elements.set(second, firstA);
        reloadInternally(elements);
    }

    public int size()
    {
        return getArray().length;
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    private void reloadInternally(Collection<? extends Authenticator> c)
    {
        array = c.toArray(new Authenticator[0]);
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        final Authenticator[] view = getArray();
        for (int index = 0; index < view.length; index++)
        {
            final Authenticator authenticator = view[index];
            builder.append(Arrays.toString(new Object[]{ index,
                                                         authenticator.getName() }));
        }
        return builder.toString();
    }

    @Override
    public Iterator<Authenticator> iterator()
    {
        return new COWIterator(getArray(), 0);
    }

    public synchronized void drop(final Class<? extends Authenticator> authenticatorClass)
    {
        List<Authenticator> elements = new ArrayList<>(Arrays.asList(getArray()));
        elements.stream().filter(e -> e.getClass().isAssignableFrom(authenticatorClass))
                .findFirst()
                .ifPresent(e ->
                           {
                               elements.remove(e);
                               reloadInternally(elements);
                           });
    }

    static final class COWIterator
            implements Iterator<Authenticator>
    {

        private final Authenticator[] snapshot;

        private int cursor;

        private COWIterator(Authenticator[] elements,
                            int initialCursor)
        {
            cursor = initialCursor;
            snapshot = Arrays.copyOf(elements, elements.length);
        }

        @Override
        public boolean hasNext()
        {
            return cursor < snapshot.length;
        }

        @Override
        public Authenticator next()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }
            return snapshot[cursor++];
        }
    }
}
