package org.carlspring.strongbox.services;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.data.service.CrudService;

import java.util.Optional;

/**
 * @author Przemyslaw Fusik
 */
public interface ConfigurationService
        extends CrudService<Configuration, String>
{

    Optional<Configuration> findOne();

}
