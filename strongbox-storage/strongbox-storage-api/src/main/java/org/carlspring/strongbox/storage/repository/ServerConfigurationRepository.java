package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.data.repository.OrientRepository;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Alex Oreshkevich
 */
@Transactional
public interface ServerConfigurationRepository extends OrientRepository<Configuration>
{

}
