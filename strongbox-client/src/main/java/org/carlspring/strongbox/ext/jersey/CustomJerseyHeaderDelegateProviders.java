package org.carlspring.strongbox.ext.jersey;

import javax.inject.Singleton;

import java.util.HashSet;
import java.util.Set;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.message.internal.*;
import org.glassfish.jersey.spi.HeaderDelegateProvider;

/**
 * @author Przemyslaw Fusik
 *
 * @see MessagingBinders.HeaderDelegateProviders
 */
public class CustomJerseyHeaderDelegateProviders
        extends AbstractBinder
{

    private final Set<HeaderDelegateProvider> providers;

    public CustomJerseyHeaderDelegateProviders()
    {
        Set<HeaderDelegateProvider> providers = new HashSet<>();
        providers.add(new CacheControlProvider());
        providers.add(new CookieProvider());
        providers.add(new DateProvider());
        providers.add(new EntityTagProvider());
        providers.add(new LinkProvider());
        providers.add(new LocaleProvider());
        providers.add(new CustomJerseyMediaTypeProvider());
        providers.add(new NewCookieProvider());
        providers.add(new StringHeaderProvider());
        providers.add(new UriProvider());
        this.providers = providers;
    }

    @Override
    protected void configure()
    {
        providers.stream().map(provider -> bind(provider).to(HeaderDelegateProvider.class).in(Singleton.class));

    }

    public Set<HeaderDelegateProvider> getProviders()
    {
        return providers;
    }

}
