package org.carlspring.strongbox.authentication.external;

import javax.xml.bind.annotation.XmlTransient;

import org.springframework.context.support.GenericApplicationContext;

/**
 * @author Przemyslaw Fusik
 */
@XmlTransient
public abstract class ExternalUserProvider
{

    public abstract void appContext(GenericApplicationContext applicationContext);

    /**
     * Register a singleton bean with the underlying bean factory.
     */
    public void registerSingleton(GenericApplicationContext applicationContext,
                                  Object singletonObject)
    {
        applicationContext.getDefaultListableBeanFactory().registerSingleton(
                singletonObject.getClass().getCanonicalName(), singletonObject);
    }
}
