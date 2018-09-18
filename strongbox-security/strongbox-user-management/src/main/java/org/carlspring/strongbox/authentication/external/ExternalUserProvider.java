package org.carlspring.strongbox.authentication.external;

import org.springframework.context.support.GenericApplicationContext;

/**
 * @author Przemyslaw Fusik
 */
public abstract class ExternalUserProvider
{

    public abstract void registerInApplicationContext(GenericApplicationContext applicationContext);

    /**
     * Register a singleton bean with the underlying bean factory.
     */
    protected void registerSingleton(GenericApplicationContext applicationContext,
                                     Object singletonObject)
    {
        applicationContext.getDefaultListableBeanFactory().registerSingleton(
                singletonObject.getClass().getCanonicalName(), singletonObject);
    }
}
