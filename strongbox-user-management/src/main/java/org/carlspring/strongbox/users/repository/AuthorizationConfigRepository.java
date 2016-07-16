package org.carlspring.strongbox.users.repository;

import org.carlspring.strongbox.data.repository.OrientRepository;
import org.carlspring.strongbox.users.security.AuthorizationConfig;

import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data CRUD repository for {@link AuthorizationConfig}.
 *
 * @author Alex Oreshkevich
 */
@Transactional
public interface AuthorizationConfigRepository
        extends OrientRepository<AuthorizationConfig>
{

}

