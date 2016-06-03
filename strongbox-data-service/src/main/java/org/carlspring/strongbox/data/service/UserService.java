package org.carlspring.strongbox.data.service;

import org.carlspring.strongbox.data.domain.User;

import java.util.Optional;

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
    Optional<User> findByUserName(final String username);
}
