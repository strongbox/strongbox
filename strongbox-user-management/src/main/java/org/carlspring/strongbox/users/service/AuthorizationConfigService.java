package org.carlspring.strongbox.users.service;

import org.carlspring.strongbox.data.service.CrudService;
import org.carlspring.strongbox.users.security.AuthorizationConfig;

import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD service for managing {@link AuthorizationConfig} entities.
 *
 * @author Alex Oreshkevich
 */
@Transactional
public interface AuthorizationConfigService
        extends CrudService<AuthorizationConfig, String>
{

}
