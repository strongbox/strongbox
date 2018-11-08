package org.carlspring.strongbox.authentication.registry;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.inject.Inject;

import org.carlspring.strongbox.authentication.registry.support.ExternalAuthenticatorsHelper;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class AuthenticationProvidersRegistry
        implements Iterable<AuthenticationProvider>
{

    private static final String PROPERTY_AUTHENTICATION_PROVIDERS_LOCATION = "strongbox.authentication.providers.xml";

    private static final String DEFAULT_AUTHENTICATION_PROVIDERS_LOCATION = "classpath:strongbox-authentication-providers.xml";

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationProvidersRegistry.class);

    private volatile AuthenticationProvider[] array;

    @Inject
    private ApplicationContext parentApplicationContext;

    /**
     * Creates an empty registry.
     */
    public AuthenticationProvidersRegistry()
    {
        reloadInternally(Collections.emptyList());
    }

    /**
     * Creates a registry containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     */
    public AuthenticationProvidersRegistry(Collection<? extends AuthenticationProvider> c)
    {
        reloadInternally(c);
    }

    private AuthenticationProvider[] getArray()
    {
        return array;
    }

    public void scanAndReloadRegistry()
    {
        final ClassLoader entryClassLoader = parentApplicationContext.getClassLoader();
        final ClassLoader requiredClassLoader = ExternalAuthenticatorsHelper.getExternalAuthenticatorsClassLoader(
                                                                                                                  entryClassLoader);

        logger.debug("Reloading authenticators registry ...");

        final AuthenticationConfigurationContext applicationContext = new AuthenticationConfigurationContext();
        try
        {
            applicationContext.setParent(parentApplicationContext);
            applicationContext.setClassLoader(requiredClassLoader);
            applicationContext.load(getAuthenticationConfigurationResource());
            applicationContext.refresh();
        }
        catch (Exception e)
        {
            logger.error("Unable to load authenticators from configuration file.", e);

            throw new UndeclaredThrowableException(e);
        }

        reload(applicationContext.getBeansOfType(AuthenticationProvider.class).values());
    }

    private Resource getAuthenticationConfigurationResource()
        throws IOException
    {
        return ConfigurationResourceResolver.getConfigurationResource(PROPERTY_AUTHENTICATION_PROVIDERS_LOCATION,
                                                                      DEFAULT_AUTHENTICATION_PROVIDERS_LOCATION);
    }

    /**
     * Replaces the authenticator of the same class
     * or adds given authenticator to the end of the authenticator lists
     */
    public synchronized void put(AuthenticationProvider authenticator)
    {
        List<AuthenticationProvider> elements = new ArrayList<>(Arrays.asList(getArray()));
        Optional<AuthenticationProvider> opt = elements.stream()
                                                       .filter(e -> e.getClass()
                                                                     .isAssignableFrom(authenticator.getClass()))
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
     * @param c
     *            new collection of authenticators
     */
    public synchronized void reload(Collection<? extends AuthenticationProvider> c)
    {
        reloadInternally(c);
    }

    /**
     * Reorders elements in the registry.
     */
    public synchronized void reorder(int first,
                                     int second)
    {
        List<AuthenticationProvider> elements = new ArrayList<>(Arrays.asList(getArray()));
        final AuthenticationProvider firstA = elements.get(first);
        final AuthenticationProvider secondA = elements.get(second);
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

    private void reloadInternally(Collection<? extends AuthenticationProvider> c)
    {
        array = c.toArray(new AuthenticationProvider[0]);
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        final AuthenticationProvider[] view = getArray();
        for (int index = 0; index < view.length; index++)
        {
            final AuthenticationProvider authenticator = view[index];
            builder.append(Arrays.toString(new Object[] { index,
                                                          authenticator.getClass().getSimpleName() }));
        }
        return builder.toString();
    }

    @Override
    public Iterator<AuthenticationProvider> iterator()
    {
        return new COWIterator(getArray(), 0);
    }

    public synchronized void drop(final Class<? extends AuthenticationProvider> authenticatorClass)
    {
        List<AuthenticationProvider> elements = new ArrayList<>(Arrays.asList(getArray()));
        elements.stream()
                .filter(e -> e.getClass().isAssignableFrom(authenticatorClass))
                .findFirst()
                .ifPresent(e -> {
                    elements.remove(e);
                    reloadInternally(elements);
                });
    }

    static final class COWIterator
            implements Iterator<AuthenticationProvider>
    {

        private final AuthenticationProvider[] snapshot;

        private int cursor;

        private COWIterator(AuthenticationProvider[] elements,
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
        public AuthenticationProvider next()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }
            return snapshot[cursor++];
        }
    }
    
    public static class AuthenticationConfigurationContext extends GenericXmlApplicationContext {

        public AuthenticationConfigurationContext()
        {
        }

    }
}
