package org.carlspring.strongbox.data.service.impl;

import org.carlspring.strongbox.data.domain.GenericEntity;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.springframework.stereotype.Component;

@Component
public class GenericEntityCrudService extends CommonCrudService<GenericEntity>
{

    @Override
    public Class<GenericEntity> getEntityClass()
    {
        return GenericEntity.class;
    }

}
