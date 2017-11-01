package org.carlspring.strongbox.data.service.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.carlspring.strongbox.data.domain.GenericEntity;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.data.service.CrudService;
import org.springframework.stereotype.Component;

@Component
public class EntityServiceRegistry
{

    private Map<Class, CommonCrudService> entityServiceMap = new ConcurrentHashMap<>();

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
