package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.configuration.BinaryConfiguration;
import org.carlspring.strongbox.data.repository.OrientRepository;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Alex Oreshkevich
 */
@Transactional
public interface ServerConfigurationRepository
        extends OrientRepository<BinaryConfiguration>
{

}
