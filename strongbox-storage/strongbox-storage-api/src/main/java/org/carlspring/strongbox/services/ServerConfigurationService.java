package org.carlspring.strongbox.services;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.data.service.CrudService;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Alex Oreshkevich
 */
@Transactional
public interface ServerConfigurationService extends CrudService<Configuration, String>
{
}
