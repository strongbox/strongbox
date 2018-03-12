package org.carlspring.strongbox.users.service;

import org.carlspring.strongbox.data.service.SingletonCrudService;
import org.carlspring.strongbox.users.security.AuthorizationConfig;

/**
 * CRUD service for managing {@link AuthorizationConfig} entities.
 *
 * @author Alex Oreshkevich
 */
public interface AuthorizationConfigService
        extends SingletonCrudService<AuthorizationConfig, String>
{

}
