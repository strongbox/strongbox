package org.carlspring.strongbox.data.service;

import org.carlspring.strongbox.data.domain.PoolConfiguration;
import org.carlspring.strongbox.data.repository.PoolConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author korest
 */
@Service
@Transactional
public class PoolConfigurationServiceImpl implements PoolConfigurationService
{

    private static final Logger LOGGER = LoggerFactory.getLogger(PoolConfigurationServiceImpl.class);

    @Autowired
    PoolConfigurationRepository poolConfigurationRepository;

    @Override
    public PoolConfiguration createOrUpdateNumberOfConnectionsForRepository(String repositoryUrl, int maxConnections)
    {
        PoolConfiguration poolConfiguration = new PoolConfiguration();

        Optional<PoolConfiguration> dbPoolConfigurationOptional = findByRepositoryUrl(repositoryUrl);
        dbPoolConfigurationOptional.ifPresent(dbPoolConfiguration -> {
            poolConfiguration.setId(dbPoolConfiguration.getId());
        });

        poolConfiguration.setRepositoryUrl(repositoryUrl);
        poolConfiguration.setMaxConnections(maxConnections);

        return poolConfigurationRepository.save(poolConfiguration);
    }

    @Override
    public Optional<PoolConfiguration> findByRepositoryUrl(String repositoryUrl)
    {
        try
        {
            PoolConfiguration poolConfiguration = poolConfigurationRepository.findByRepositoryUrl(repositoryUrl);
            return Optional.ofNullable(poolConfiguration);
        }
        catch (Exception e)
        {
            LOGGER.error(e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Iterable<PoolConfiguration>> findAll()
    {
        return Optional.ofNullable(poolConfigurationRepository.findAll());
    }
}
