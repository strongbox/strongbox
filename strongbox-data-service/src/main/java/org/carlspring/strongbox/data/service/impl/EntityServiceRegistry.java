package org.carlspring.strongbox.data.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.carlspring.strongbox.data.domain.GenericEntity;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.data.service.CrudService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public class EntityServiceRegistry implements BeanFactoryPostProcessor
{

    private Map<Class, CommonCrudService> entityServiceMap = new ConcurrentHashMap<>();
    private volatile List<String> crudServiceBeanList;
    private ConfigurableListableBeanFactory beanFactory;

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
        throws BeansException
    {
        crudServiceBeanList = Arrays.asList(beanFactory.getBeanNamesForType(CommonCrudService.class));
        this.beanFactory = beanFactory;
    }

    public <T extends GenericEntity> void register(Class<T> entityClass,
                                                   CommonCrudService<T> commonCrudService)
    {
        entityServiceMap.putIfAbsent(entityClass, commonCrudService);
    }

    public <T extends GenericEntity> CommonCrudService<T> getEntityService(Class<T> entityClass)
    {
        if (crudServiceBeanList != null)
        {
            init();
        }
        Class<?> t = entityClass;
        do
        {
            CrudService result = entityServiceMap.get(t);
            if (result != null)
            {
                return (CommonCrudService<T>) result;
            }
            t = t.getSuperclass();
        } while (!t.equals(Object.class));

        throw new RuntimeException(String.format("Failed to locate service for [%s]", entityClass));
    }

    private synchronized void init()
    {
        if (crudServiceBeanList == null)
        {
            return;
        }
        List<String> crudServiceBeanListLocal = crudServiceBeanList;
        crudServiceBeanList = null;
        crudServiceBeanListLocal.stream().forEach((b) -> beanFactory.getBean(b));
    }
}
