package org.carlspring.strongbox.services;

import org.carlspring.strongbox.configuration.BinaryConfiguration;
import org.carlspring.strongbox.data.service.CrudService;

import java.util.Optional;

/**
 * @author Przemyslaw Fusik
 */
public interface BinaryConfigurationService
        extends CrudService<BinaryConfiguration, String>
{

    Optional<BinaryConfiguration> findOne();

}
