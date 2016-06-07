package org.carlspring.strongbox.users.repository;

import org.carlspring.strongbox.data.repository.OrientRepository;
import org.carlspring.strongbox.users.domain.User;

import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data CRUD repository for {@link User}.
 *
 * @author Alex Oreshkevich
 */
@Transactional
public interface UserRepository
        extends OrientRepository<User>
{

    // select * from User where username = 'admin'
    User findByUsername(String userName);

}

