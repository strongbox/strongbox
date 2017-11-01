package org.carlspring.strongbox.data.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.carlspring.strongbox.data.domain.GenericEntity;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.data.service.CrudService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public class EntityServiceRegistry
{

    private Map<Class, CommonCrudService> entityServiceMap = new ConcurrentHashMap<>();

    @Component
    public static class EntityServiceRegistryBootstrap implements BeanFactoryPostProcessor
    {
        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException
        {
            BeanDefinition genericEntityCrudService = beanFactory.getBeanDefinition("genericEntityCrudService");
            List<String> crudServiceBeanList = Arrays.asList(beanFactory.getBeanNamesForType(CommonCrudService.class));
            genericEntityCrudService.setDependsOn(crudServiceBeanList.stream()
                                                                     .filter(b1 -> !b1.equals("genericEntityCrudService"))
                                                                     .collect(Collectors.toList())
                                                                     .toArray(new String[crudServiceBeanList.size()
                                                                             - 1]));
        }
    }

    public <T extends GenericEntity> void register(Class<T> entityClass,
                                                   CommonCrudService<T> commonCrudService)
    {
        entityServiceMap.putIfAbsent(entityClass, commonCrudService);
    }

    public <T extends GenericEntity> CommonCrudService<T> getEntityService(Class<T> entityClass)
    {
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
}
