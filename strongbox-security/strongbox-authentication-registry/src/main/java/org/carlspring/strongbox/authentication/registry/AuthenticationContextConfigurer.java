package org.carlspring.strongbox.authentication.registry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

public class AuthenticationContextConfigurer implements BeanFactoryPostProcessor
{

    private Environment env;

    public void setEnvironment(ConfigurableEnvironment env)
    {
        this.env = env;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
        throws BeansException
    {

        for (String beanName : beanFactory.getBeanDefinitionNames())
        {
            String configurationProperty = String.format("strongbox.authentication.%s.enabled", beanName);
            if (!env.containsProperty(configurationProperty))
            {
                continue;
            }

            Boolean enabled = env.getProperty(configurationProperty, Boolean.class, true);
            if (Boolean.FALSE.equals(enabled))
            {
                ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(beanName);
            }
        }

    }

}
