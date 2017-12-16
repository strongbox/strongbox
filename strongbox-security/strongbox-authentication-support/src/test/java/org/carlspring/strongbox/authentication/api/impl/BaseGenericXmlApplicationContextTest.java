package org.carlspring.strongbox.authentication.api.impl;


import javax.inject.Inject;
import java.io.IOException;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.Resource;

/**
 * @author Przemyslaw Fusik
 */
public abstract class BaseGenericXmlApplicationContextTest
{

    protected GenericXmlApplicationContext appCtx;

    @Inject
    private ApplicationContext parentApplicationContext;

    @Before
    public void initAppContext()
            throws IOException
    {
        if (appCtx == null)
        {
            appCtx = new GenericXmlApplicationContext();
            appCtx.setParent(parentApplicationContext);
            appCtx.load(getAuthenticationConfigurationResource());
            appCtx.refresh();
        }
    }

    @After
    public void closeAppContext()
    {
        if (appCtx != null)
        {
            appCtx.close();
            appCtx = null;
        }
    }


    protected void displayBeanNames()
    {
        if (appCtx != null)
        {
            Stream.of(appCtx.getBeanDefinitionNames())
                  .forEach(getLogger()::debug);
        }
    }

    protected abstract Resource getAuthenticationConfigurationResource()
            throws IOException;

    protected abstract Logger getLogger();

}
