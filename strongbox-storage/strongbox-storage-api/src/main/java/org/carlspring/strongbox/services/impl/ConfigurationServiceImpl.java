package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.data.criteria.DetachQueryTemplate;
import org.carlspring.strongbox.data.service.SingletonCommonCrudService;
import org.carlspring.strongbox.services.ConfigurationService;

import javax.transaction.Transactional;
import java.util.Optional;

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

    @Override
    public Optional<Configuration> findOne()
    {
        Optional<Configuration> result = super.findOne();
        if (!result.isPresent())
        {
            return result;
        }
        return Optional.of((Configuration) new DetachQueryTemplate(entityManager).unproxy(result.get()));
    }
}
