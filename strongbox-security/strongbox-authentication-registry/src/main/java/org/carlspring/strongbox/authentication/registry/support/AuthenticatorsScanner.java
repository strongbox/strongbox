package org.carlspring.strongbox.authentication.registry.support;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.external.ExternalUserProvider;
import org.carlspring.strongbox.authentication.external.ExternalUserProviders;
import org.carlspring.strongbox.authentication.external.ExternalUserProvidersFileManager;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Throwables;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.Resource;

/**
 * @author Przemyslaw Fusik
 */
public class AuthenticatorsScanner
{

    private static final Logger logger = LoggerFactory.getLogger(AuthenticatorsScanner.class);

    private final AuthenticatorsRegistry registry;

    @Inject
    private ApplicationContext parentApplicationContext;

    @Inject
    private ExternalUserProvidersFileManager externalUserProvidersFileManager;


    public AuthenticatorsScanner(AuthenticatorsRegistry registry)
    {
        this.registry = registry;
    }

    public void scanAndReloadRegistry()
    {
        final ClassLoader entryClassLoader = parentApplicationContext.getClassLoader();
        final ClassLoader requiredClassLoader = ExternalAuthenticatorsHelper.getExternalAuthenticatorsClassLoader(
                entryClassLoader);

        logger.debug("Reloading authenticators registry ...");

        final GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext();
        try
        {
            applicationContext.setParent(parentApplicationContext);
            applicationContext.setClassLoader(requiredClassLoader);
            loadExternalUserProvidersConfiguration(applicationContext);
            applicationContext.load(getAuthenticationConfigurationResource());
            applicationContext.refresh();
        }
        catch (Exception e)
        {
            logger.error("Unable to load authenticators from configuration file.", e);

            throw new UndeclaredThrowableException(e);
        }

        final List<Authenticator> authenticators = getAuthenticators(applicationContext);

        logger.debug("Scanned authenticators: {}",
                     authenticators.stream().map(Authenticator::getName).collect(Collectors.toList()));

        registry.reload(authenticators);
    }

    private void loadExternalUserProvidersConfiguration(final GenericXmlApplicationContext applicationContext)
    {
        ExternalUserProviders externalUserProviders = externalUserProvidersFileManager.read();
        if (externalUserProviders != null)
        {
            Set<ExternalUserProvider> providers = externalUserProviders.getProviders();
            if (CollectionUtils.isNotEmpty(providers))
            {
                providers.stream().forEach(p -> p.registerInApplicationContext(applicationContext));
            }
        }
    }

    private List<Authenticator> getAuthenticators(ApplicationContext applicationContext)
    {
        final List<Object> authenticatorsClasses = applicationContext.getBean("authenticators", List.class);
        final List<Authenticator> authenticators = new ArrayList<>();
        for (final Object authenticator : authenticatorsClasses)
        {
            Class<?> authenticatorClass = authenticator.getClass();
            if (!Authenticator.class.isAssignableFrom(authenticatorClass))
            {
                throw new IllegalAuthenticatorException(authenticatorClass + " is not assignable from " +
                                                        Authenticator.class.getName());
            }

            authenticators.add((Authenticator) authenticator);
        }

        return authenticators;
    }

    private Resource getAuthenticationConfigurationResource()
            throws IOException
    {
        return ConfigurationResourceResolver.getConfigurationResource("strongbox.authentication.providers.xml",
                                                                      "etc/conf/strongbox-authentication-providers.xml");
    }

    @EventListener({ ContextRefreshedEvent.class })
    void contextRefreshedEvent(ContextRefreshedEvent e)
    {
        if (parentApplicationContext != e.getApplicationContext())
        {
            return;
        }

        scanAndReloadRegistry();
    }

}
