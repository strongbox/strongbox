package org.carlspring.strongbox.data.repository;

import org.carlspring.strongbox.data.domain.PoolConfiguration;

/**
 * @author korest
 */
public interface PoolConfigurationRepository extends OrientRepository<PoolConfiguration>
{

    // select * from PoolConfiguration where repositoryUrl = 'repositoryUrl'
    PoolConfiguration findByRepositoryUrl(String repositoryUrl);
}
