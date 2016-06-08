package org.carlspring.strongbox.data.service;

import org.carlspring.strongbox.data.domain.PoolConfiguration;

import java.util.Optional;

/**
 * @author korest
 */
public interface PoolConfigurationService
{

    PoolConfiguration createOrUpdateNumberOfConnectionsForRepository(String repositoryUrl, int maxConnections);
    Optional<PoolConfiguration> findByRepositoryUrl(String repositoryUrl);
    Optional<Iterable<PoolConfiguration>> findAll();

}
