package org.carlspring.strongbox.users.service.impl;

import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.security.AuthorizationConfig;
import org.carlspring.strongbox.users.service.AuthorizationConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO implementation for {@link User} entities.
 *
 * @author Alex Oreshkevich
 */
@Service
@Transactional
public class AuthorizationConfigServiceImpl extends CommonCrudService<AuthorizationConfig>
        implements AuthorizationConfigService
{

    @Override
    public Class<AuthorizationConfig> getEntityClass()
    {
        return AuthorizationConfig.class;   
    }

}
