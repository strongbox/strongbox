package org.carlspring.strongbox.users.service;

import org.carlspring.strongbox.data.service.CrudService;
import org.carlspring.strongbox.users.domain.User;

import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD service for managing {@link User} entities.
 *
 * @author Alex Oreshkevich
 */
@Transactional
public interface UserService
        extends CrudService<User, String>
{

    @Transactional
    User findByUserName(final String username);
}
