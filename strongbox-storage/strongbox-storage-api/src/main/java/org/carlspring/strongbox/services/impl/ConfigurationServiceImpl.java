package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.services.ConfigurationService;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * @author Przemyslaw Fusik
 */
@Transactional
@Service
public class ConfigurationServiceImpl
        extends CommonCrudService<Configuration>
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
        final Optional<List<Configuration>> all = findAll();
        if (!all.isPresent())
        {
            return Optional.empty();
        }
        final List<Configuration> configurations = all.get();
        if (configurations.size() != 1)
        {
            throw new IllegalStateException("Found more than one Configuration");
        }
        return Optional.of(configurations.get(0));
    }
}
