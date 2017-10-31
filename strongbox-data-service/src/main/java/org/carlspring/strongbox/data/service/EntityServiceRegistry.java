package org.carlspring.strongbox.data.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.carlspring.strongbox.data.domain.GenericEntity;
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
        CrudService result = entityServiceMap.get(entityClass);
        if (result == null)
        {
            throw new RuntimeException(String.format("Failed to resolve service for [%s]", entityClass));
        }
        return (CommonCrudService<T>) result;
    }
}
