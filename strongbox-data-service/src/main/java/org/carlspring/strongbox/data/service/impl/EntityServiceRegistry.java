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
        CrudService result = entityServiceMap.get(entityClass);
        if (result == null)
        {
            return entityServiceMap.get(GenericEntity.class);
        }
        return (CommonCrudService<T>) result;
    }
}
