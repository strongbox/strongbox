package org.carlspring.strongbox.data.repository;

import org.carlspring.strongbox.data.domain.StrongboxUser;

import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data CRUD repository for {@link StrongboxUser}.
 *
 * @author Alex Oreshkevich
 */
@Transactional
public interface StrongboxUserRepository
        extends OrientRepository<StrongboxUser>
{

    // select * from StrongboxUser where username = 'admin'
    StrongboxUser findByUsername(String userName);
}

