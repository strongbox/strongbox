package org.carlspring.strongbox.authentication.registry.support;

import org.carlspring.strongbox.authentication.XmlAuthenticationExtraBean;
import org.carlspring.strongbox.authentication.XmlAuthenticationProviders;
import org.carlspring.strongbox.authentication.XmlAuthenticator;
import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.api.Authenticators;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.authentication.registry.support.xml.AuthenticationProvidersFileManager;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Throwables;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.StaticApplicationContext;

/**
 * @author Przemyslaw Fusik
 */
public class AuthenticatorsScanner
{

    private static final Logger logger = LoggerFactory.getLogger(AuthenticatorsScanner.class);

    private final AuthenticatorsRegistry registry;

    @Inject
    private AuthenticationProvidersFileManager authenticationProvidersFileManager;

    @Inject
    private ApplicationContext parentApplicationContext;

    private XmlAuthenticationProviders authenticationProviders;

    public AuthenticatorsScanner(AuthenticatorsRegistry registry)
    {
        this.registry = registry;
    }

    @PostConstruct
    void init()
    {
        authenticationProviders = authenticationProvidersFileManager.read();
    }

    public void scanAndReloadRegistry()
    {
        final ClassLoader entryClassLoader = parentApplicationContext.getClassLoader();
        final ClassLoader requiredClassLoader = ExternalAuthenticatorsHelper.getExternalAuthenticatorsClassLoader(
                entryClassLoader);

        logger.debug("Reloading authenticators registry ...");
        final StaticApplicationContext applicationContext = new StaticApplicationContext();
        try
        {
            applicationContext.setParent(parentApplicationContext);
            applicationContext.setClassLoader(requiredClassLoader);
            initializeContext(applicationContext);
            applicationContext.refresh();
        }
        catch (Exception e)
        {
            logger.error("Unable to load authenticators from configuration file.", e);
            throw Throwables.propagate(e);
        }

        final List<Authenticator> authenticators = getAuthenticators(applicationContext);
        logger.debug("Scanned authenticators: {}", authenticators.stream().map(Authenticator::getName).collect(
                Collectors.toList()));
        registry.reload(authenticators);
    }

    private List<Authenticator> getAuthenticators(ApplicationContext applicationContext)
    {
        final Authenticators authenticators = applicationContext.getBean(Authenticators.class.getSimpleName(),
                                                                         Authenticators.class);
        return authenticators.getAuthenticators();
    }

    private void initializeContext(final StaticApplicationContext applicationContext)
            throws ClassNotFoundException
    {
        applicationContext.registerSingleton(AutowiredAnnotationBeanPostProcessor.class.getSimpleName(),
                                             AutowiredAnnotationBeanPostProcessor.class);

        DefaultListableBeanFactory dlbf = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
        dlbf.setAutowireCandidateResolver(new QualifierAnnotationAutowireCandidateResolver());

        if (authenticationProviders.getAuthenticators() != null &&
            !CollectionUtils.isEmpty(authenticationProviders.getAuthenticators().getAuthenticators()))
        {
            for (XmlAuthenticator authenticator : authenticationProviders.getAuthenticators().getAuthenticators())
            {
                Class<?> authenticatorClass = applicationContext.getClassLoader().loadClass(
                        authenticator.getClassName());
                applicationContext.registerSingleton(authenticatorClass.getSimpleName(), authenticatorClass);
            }
            applicationContext.registerSingleton(Authenticators.class.getSimpleName(), Authenticators.class);
        }

        if (authenticationProviders.getExtraBeans() != null &&
            !CollectionUtils.isEmpty(authenticationProviders.getExtraBeans().getExtraBeans()))
        {
            for (XmlAuthenticationExtraBean extraBean : authenticationProviders.getExtraBeans().getExtraBeans())
            {
                Class<?> extraBeanClass = applicationContext.getClassLoader().loadClass(extraBean.getClassName());
                applicationContext.registerSingleton(extraBeanClass.getSimpleName(), extraBeanClass);
            }
        }
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
