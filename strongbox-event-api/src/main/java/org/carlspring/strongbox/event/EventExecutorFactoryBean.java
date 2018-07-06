package org.carlspring.strongbox.event;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.Executor;

import javax.servlet.ServletContext;

import org.apache.catalina.Service;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.data.util.ReflectionUtils;

public class EventExecutorFactoryBean implements FactoryBean<Executor>
{

    private static final String STRONGBOX_HTTP_REQUEST_EXECUTOR = "strongbox.httpRequestExecutor";

    private static final Logger logger = LoggerFactory.getLogger(EventExecutorFactoryBean.class);

    private final ServletContext servletContext;

    public EventExecutorFactoryBean(ServletContext servletContext)
    {
        super();
        this.servletContext = servletContext;
    }

    @Override
    public Executor getObject()
        throws Exception
    {
        Executor executor = Optional.ofNullable(servletContext)
                                    .flatMap(c -> Optional.ofNullable(lookupExecutor()))
                                    .orElse(new SyncTaskExecutor());
        
        logger.info(String.format("Using [%s] executor for Async events.", executor.getClass()));
        
        return executor;
    }

    private Executor lookupExecutor()
    {
        Executor result;
        if ((result = lookupJettyExecutor()) != null)
        {
            return result;
        }
        else if ((result = lookupTomcatExecutor()) != null)
        {
            return result;
        }
        return null;
    }

    private Executor lookupJettyExecutor()
    {
        Executor executor = (Executor) servletContext.getAttribute("org.eclipse.jetty.server.Executor");
        if (executor == null)
        {
            return null;
        }

        logger.info("Jetty environment detected.");

        return executor;
    }

    private Executor lookupTomcatExecutor()
    {
        try
        {
            Class.forName("org.apache.catalina.core.ApplicationContextFacade");
        }
        catch (ClassNotFoundException e)
        {
            return null;
        }

        logger.info("Tomcat environment detected.");

        Service service;
        try
        {
            Field field = ReflectionUtils.findField(servletContext.getClass(),
                                                    (f) -> f.getName().equals("sc"));
            field.setAccessible(true);

            ApplicationContextFacade asc = (ApplicationContextFacade) field.get(servletContext);

            field = ReflectionUtils.findField(ApplicationContextFacade.class,
                                              (f) -> f.getName().equals("context"));
            field.setAccessible(true);

            ApplicationContext context = (ApplicationContext) field.get(asc);

            field = ReflectionUtils.findField(ApplicationContext.class, (f) -> f.getName().equals("service"));
            field.setAccessible(true);

            service = (Service) field.get(context);
        }
        catch (IllegalAccessException e)
        {
            logger.error(String.format("Failed to get Tomcat Service: [%s] ", e.getMessage()));

            return null;
        }

        Executor executor = service.getExecutor(STRONGBOX_HTTP_REQUEST_EXECUTOR);
        if (executor == null)
        {
            logger.warn(String.format("Executor [%s] not found in Tomcat environment, you need to expectedly define it in `server.xml` configuration file.",
                                      STRONGBOX_HTTP_REQUEST_EXECUTOR));
            return null;
        }

        return executor;
    }

    @Override
    public Class<?> getObjectType()
    {
        return Executor.class;
    }

}
