package org.carlspring.strongbox.app.boot;

import org.carlspring.strongbox.web.DirectoryTraversalFilter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletRegistration;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ApplicationListenerMethodAdapter;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.stereotype.Component;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Component
@Order(HIGHEST_PRECEDENCE)
public class BootProgressBeanPostProcessor
        implements BeanPostProcessor, BeanNameAware, ApplicationContextAware
{

    private static final Logger logger = LoggerFactory.getLogger(BootProgressBeanPostProcessor.class);

    private static Subject<String> progress;

    private ApplicationContext applicationContext;

    private volatile String beanName;

    private volatile WebServer webServer;

    //@formatter:off
    private static final Map<String, String> displayMessages = Stream.of(new String[][]{
            { "management.metrics-org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties", "Loading metrics..." },
            { "orientDbServer", "Waiting for database..." },
            { "liquibase", "Running update scripts..." },
            { org.carlspring.strongbox.config.hazelcast.HazelcastConfiguration.class.getName(), "Waiting for cache..." },
            { "org.springframework.boot.actuate.autoconfigure", "Waiting for actuators..." },
            // it might look like we need to use "storageBooter" here, but that actually depends on tempDirBooter which does most of the heavy lifting.
            { "tempDirBooter", "Waiting for storage booter" },
            { "storageProviderRegistry", "Loading storage providers..." },
            { "maven2LayoutProvider", "Loading Maven layout provider.." },
            { "nugetLayoutProvider", "Loading Nuget layout provider.." },
            { "npmLayoutProvider", "Loading NPM layout provider.." },
            { "rawLayoutProvider", "Loading Raw layout provider.." },
            { "transactionManager", "Loading transaction manager..." },
            { org.carlspring.strongbox.config.WebSecurityConfig.class.getName(), "Loading security configuration..." },
            { org.carlspring.strongbox.config.SwaggerConfig.class.getName(), "Loading documentation..." },
            { "fallback", "Waiting for services to go live..."},
    }).collect(Collectors.toMap(p -> p[0], p -> p[1]));
    //@formatter:on

    public static Observable<String> getProgressObservable()
    {
        return progress.observeOn(Schedulers.single()).subscribeOn(Schedulers.io()).onTerminateDetach();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanName(String beanName)
    {
        this.beanName = beanName;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event)
    {
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        factory.destroySingleton(beanName);
    }

    @PostConstruct
    public void startup() {
        progress = BehaviorSubject.create();
    }

    @PreDestroy
    public void destroy()
            throws InterruptedException
    {
        logger.debug("Notifying clients Strongbox has booted.");
        progress.onComplete();
        progress = null;

        stopServer();
        removeEventListener();
    }

    private void stopServer()
            throws InterruptedException
    {
        // Slightly delay stopping the server to leave enough time for existing responses to complete.
        // Fixes
        //   curl: (18) transfer closed with outstanding read data remaining
        //   err_incomplete_chunked_encoding
        // NB: This needs to be in sync with the UI.
        Thread.sleep(1000);

        logger.debug("Stopping server {}", webServer.toString());
        webServer.stop();

        webServer = null;
    }

    private void removeEventListener()
    {
        logger.debug("Removing registered {} event listener", BootProgressBeanPostProcessor.class);

        Collection<ApplicationListener<?>> listeners = ((AbstractApplicationContext) applicationContext).getApplicationListeners();

        ApplicationListener<?> applicationListener = listeners.stream()
                                                              .filter(listener -> listener instanceof ApplicationListenerMethodAdapter &&
                                                                                  ((ApplicationListenerMethodAdapter) listener)
                                                                                          .toString()
                                                                                          .contains(BootProgressBeanPostProcessor.class.getSimpleName()))
                                                              .findFirst()
                                                              .get();

        ApplicationEventMulticaster applicationEventMulticaster = applicationContext.getBean(
                AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
        applicationEventMulticaster.removeApplicationListener(applicationListener);
    }

    @Nullable
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException
    {
        logger.trace("Found bean: {}", beanName);

        if(displayMessages.containsKey(beanName) || beanName.contains(DefaultAuthenticationEventPublisher.class.getName()))
        {
            String displayMessage = displayMessages.getOrDefault(beanName, displayMessages.get("fallback"));
            progress.onNext(displayMessage);
        }

        return bean;
    }

    @Override
    @Nullable
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException
    {
        if (bean instanceof JettyServletWebServerFactory)
        {
            return startServer((JettyServletWebServerFactory) bean);
        }
        else
        {
            return bean;
        }
    }

    private JettyServletWebServerFactory startServer(JettyServletWebServerFactory factory)
    {
        logger.debug("Eagerly starting web server {}", factory.toString());

        webServer = factory.getWebServer(context -> {
            ServletRegistration.Dynamic defaultServlet = context.addServlet(DefaultBootServlet.class.getName(),
                                                                            DefaultBootServlet.class);
            defaultServlet.addMapping("/");
            defaultServlet.setLoadOnStartup(1);

            ServletRegistration.Dynamic bootProgressServlet = context.addServlet(BootProgressServlet.class.getName(),
                                                                                 BootProgressServlet.class);
            bootProgressServlet.addMapping("/api/ping");
            bootProgressServlet.setAsyncSupported(true);

            context.addFilter(DirectoryTraversalFilter.class.getName(), DirectoryTraversalFilter.class);
        });

        webServer.start();

        return factory;
    }

}
