package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.BinaryConfiguration;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.services.BinaryConfigurationService;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * @author Przemyslaw Fusik
 */
@Transactional
@Service
public class BinaryConfigurationServiceImpl
        extends CommonCrudService<BinaryConfiguration>
        implements BinaryConfigurationService
{

    @Override
    public Class<BinaryConfiguration> getEntityClass()
    {
        return BinaryConfiguration.class;
    }

    @Override
    public Optional<BinaryConfiguration> findOne()
    {
        final Optional<List<BinaryConfiguration>> all = findAll();
        if (!all.isPresent())
        {
            return Optional.empty();
        }
        final List<BinaryConfiguration> configurations = all.get();
        if (configurations.size() != 1)
        {
            throw new IllegalStateException("Found more than one Configuration");
        }
        return Optional.of(configurations.get(0));
    }
}
