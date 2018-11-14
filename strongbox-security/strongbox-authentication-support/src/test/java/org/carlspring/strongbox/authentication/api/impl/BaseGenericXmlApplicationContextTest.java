package org.carlspring.strongbox.authentication.api.impl;


import org.carlspring.strongbox.authentication.external.ExternalUserProvider;
import org.carlspring.strongbox.authentication.external.ExternalUserProviders;
import org.carlspring.strongbox.authentication.external.ExternalUserProvidersFileManager;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    @Inject
    private ExternalUserProvidersFileManager externalUserProvidersFileManager;


    @BeforeEach
    public void initAppContext()
            throws IOException
    {
        if (appCtx == null)
        {
            appCtx = new GenericXmlApplicationContext();
            appCtx.setParent(parentApplicationContext);
            loadExternalUserProvidersConfiguration(appCtx);
            appCtx.load(getAuthenticationConfigurationResource());
            appCtx.refresh();
        }
    }

    @AfterEach
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

    protected abstract Resource getAuthenticationConfigurationResource()
            throws IOException;

    protected abstract Logger getLogger();

}
