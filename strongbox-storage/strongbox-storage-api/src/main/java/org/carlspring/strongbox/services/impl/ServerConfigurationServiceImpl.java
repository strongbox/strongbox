package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.BinaryConfiguration;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.services.ServerConfigurationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Alex Oreshkevich
 */
@Service
@Transactional
class ServerConfigurationServiceImpl extends CommonCrudService<BinaryConfiguration>
        implements ServerConfigurationService
{

    @Override
    public Class<BinaryConfiguration> getEntityClass()
    {
        return BinaryConfiguration.class;
    }

}
