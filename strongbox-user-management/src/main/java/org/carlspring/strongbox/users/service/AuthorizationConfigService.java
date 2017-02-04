package org.carlspring.strongbox.users.service;

import org.carlspring.strongbox.data.service.CrudService;
import org.carlspring.strongbox.users.security.AuthorizationConfig;

/**
 * CRUD service for managing {@link AuthorizationConfig} entities.
 *
 * @author Alex Oreshkevich
 */
public interface AuthorizationConfigService
        extends CrudService<AuthorizationConfig, String>
{

}
