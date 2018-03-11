package org.carlspring.strongbox.services;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.data.service.SingletonCrudService;

/**
 * @author Przemyslaw Fusik
 */
public interface ConfigurationService
        extends SingletonCrudService<Configuration, String>
{

}
