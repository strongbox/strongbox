package org.carlspring.strongbox.data.repository;

import org.carlspring.strongbox.data.domain.PoolConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author korest
 */
@Transactional
public interface PoolConfigurationRepository extends OrientRepository<PoolConfiguration>
{

    // select * from PoolConfiguration where repositoryUrl = 'repositoryUrl'
    PoolConfiguration findByRepositoryUrl(String repositoryUrl);
}
