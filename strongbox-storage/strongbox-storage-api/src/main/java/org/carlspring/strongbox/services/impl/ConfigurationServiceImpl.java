package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.data.service.SingletonCommonCrudService;
import org.carlspring.strongbox.services.ConfigurationService;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

/**
 * @author Przemyslaw Fusik
 */
@Transactional
@Service
public class ConfigurationServiceImpl
        extends SingletonCommonCrudService<Configuration>
        implements ConfigurationService
{

    @Override
    public Class<Configuration> getEntityClass()
    {
        return Configuration.class;
    }

}
